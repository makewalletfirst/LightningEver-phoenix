# 260608_zero-reserve.md — Zero-Reserve(예치금 0%) 연동 구현 전략 보고서

이 문서는 사용자가 1%의 채널 예치금(Channel Reserve) 제약 없이 지갑 잔고를 100% 한도까지 자유롭게 송금할 수 있도록, LSP(Eclair) 및 지갑 클라이언트(Phoenix/eclair-kmp) 간의 Zero-Reserve 프로토콜을 구현하기 위한 상세 전략과 코드 수정 가이드를 담고 있습니다.

---

## 1. Zero-Reserve 프로토콜 연동의 핵심 원리

라이트닝 네트워크 표준 규격상, 채널 예치금(Reserve)은 피어가 과거 상태(Revoked State)로 사기 송금을 시도할 때 이를 처벌(Punishment)하고 회수하기 위한 담보금 역할을 합니다. 
그러나 **Zero-Reserve**를 구현한다는 것은 **"LSP가 클라이언트(지갑)를 신뢰하여 담보금(Reserve)을 요구하지 않음(0%)"**을 양방향 기능 협상(Feature Negotiation)을 통해 확정 짓는 것을 의미합니다.

이를 위해 크게 **3가지 레이어**에서 수정이 진행되어야 합니다.

---

## 2. 레이어별 상세 구현 로드맵

### 레이어 A. LSP 서버 (Eclair) 수정 및 환경 설정

Eclair 서버 단에서 채널 개설 파라미터 검증 단계 및 트랜잭션 빌더에서 예치금 검증 조건을 우회하거나 0으로 설정해야 합니다.

1. **설정 파일 (`eclair.conf`) 확인**:
   * Eclair가 `phoenix_zero_reserve` 기능을 지원하도록 설정해야 합니다.
   * `eclair.features.phoenix_zero_reserve = optional` (또는 mandatory)
2. **트랜잭션 검증부 수정 (`InteractiveTxBuilder.scala` 등)**:
   * Eclair 코어 소스코드 내 `validateTx` 또는 `validateParams` 관련 검증 로직에서, 원격 피어(클라이언트)가 제안하는 `channel_reserve_satoshis`가 `0`이거나 설정된 최소 비율보다 낮을 때 거부하는 로직을 찾습니다.
   * `phoenix_zero_reserve` 피처가 활성화된 채널타입에 한해서는 `reserve = 0`을 허용하도록 예외 가드(`if (features.hasFeature(Features.PhoenixZeroReserve))`)를 추가합니다.

### 레이어 B. 클라이언트 라이브러리 (eclair-kmp / lightning-kmp) 수정

클라이언트 핵심 라이브러리인 KMP 내에서 Feature 협상 플래그를 주입하고, 채널 개설 협상 시 예치금 한도를 0으로 요구해야 합니다.

1. **기능 플래그 (Feature Flag) 활성화**:
   * `Features.kt` 또는 `InitFeatures` 선언부에서 `phoenix_zero_reserve` 피처 비트(Feature Bit)가 제대로 등록 및 양방향 활성화(Negotiation)되도록 보장합니다.
2. **채널 파라미터 협상 로직 수정 (`ChannelTypes.kt`, `DualFundingHandlers.kt`)**:
   * 채널 개설 요청(`OpenDualFundedChannel`) 혹은 수락(`AcceptDualFundedChannel`) 메시지를 생성할 때, `channelReserve` 값을 로컬 노드 파라미터에서 가져오게 됩니다.
   * 이때 `phoenix_zero_reserve` 피처가 합의된 상태라면, `channelReserveSatoshi` 계산 결과를 강제로 **`0`** 또는 최소 dust 한도로 셋팅하여 요청 메시지를 구성하게 만듭니다.
   * 예: `val localReserve = if (features.hasFeature(Feature.ZeroReserve)) 0.sat else fundingAmount * 0.01`

### 레이어 C. 클라이언트 지갑 UI (Phoenix Android) 수정

지갑의 결제 검증(Validation) 단계에서 하드코딩된 1% reserve 장벽을 걷어내야 합니다.

1. **결제 화면 검증 로직 수정 (`SendToBolt11.kt`, `SendOfferView.kt`)**:
   * 우리가 이번에 추가한 pre-flight reserve 검증 로직을 dynamic하게 개선합니다.
   * 해당 채널의 `localParams` 또는 `channelFeatures` 정보를 조사하여, `phoenix_zero_reserve` 기능이 켜져 있거나 실제 채널의 `localChannelReserve` 값이 `0`인 채널들에 대해서는 `totalReserveMsat = 0`으로 계산하도록 우회합니다.
   ```kotlin
   // zero-reserve 채널인 경우 reserve를 0으로 계산하여 100% 전액 송금 허용
   val totalReserveSat = activeChannels.sumOf { channel ->
       if (channel.commitments.features.contains(Feature.ZeroReserve) || channel.commitments.localParams.channelReserve == 0.sat) {
           0L
       } else {
           channel.commitments.latest.fundingAmount.toLong() / 100
       }
   }
   ```

---

## 3. 테스트 및 검증 전략

1. **사설 테스트넷 연동**:
   * 수정 완료 후, 폰B의 데이터를 지우고 재생성하여 LSP 서버와 채널을 새로 맺습니다.
   * 이때 채널의 타입 정보 및 상태 파라미터(`LocalParams`)에 `channelReserve = 0`이 정상적으로 설정되었는지 Eclair CLI (`/channels` API) 및 피닉스 디버그 화면을 통해 확인합니다.
2. **100% 잔고 털기 송금 테스트**:
   * 폰B에 50만 sat를 입금받은 후, 수수료를 제외한 **정확히 100%의 잔고 전액**을 폰A로 역송금 시도합니다.
   * 송금 도중 채널이 붕괴(Force-Close)되거나 LSP에서 에러를 뱉지 않고, 깔끔하게 잔고가 `0 sat`이 되면서 송금이 최종 성공하는지 검증합니다.
