# BitEver Phoenix (LightningEver) — 260517_SEND 브랜치

ACINQ Phoenix Android를 BitEver L1 체인용으로 fork한 빌드입니다. LightningEver 앱 명으로 동작.

## 달성 마일스톤

1. ✅ 외부 L1 입금 → 자동 채널 생성 (앱 화면에서 swap-in 주소 → 송금 → 채널 자동 활성화)
2. ✅ Bolt12 송금 (양측 채널 보유)
3. ✅ 지정 주소 mutual close (Settings → Channels → Close)
4. ✅ Force close (Settings → Channels → Force close, 144 블록 CSV 후 회수)
5. ✅ **채널 없는 폰에게 Bolt12 송금 → 자동 채널 생성** (Bolt12 offer만 받으면 됨, 잔액 0이어도 가능)

## 의존 라이브러리

이 빌드는 lightning-kmp `1.11.5-DEBUG` (BitEver fork)에 의존:

- 레포: `makewalletfirst/LightningEver-bitever-eclair-kmp`
- 브랜치: `260517_SEND`
- 빌드 후 `~/.m2/repository/fr/acinq/lightning/lightning-kmp-core-jvm/1.11.5-DEBUG/`에 publish 필요

## 주요 변경점 (vs upstream Phoenix)

- 앱 패키지명: `fr.acinq.phoenix.testnet` (`testnet` 표시지만 실제로는 BitEver mainnet 사용)
- 앱 표시명: `LightningEver`
- LSP 노드 ID: `0311fb42898ef97d86e5b4e2615040edd91e1ca350cea6e573b7af080fba105e07`
- LSP IP: `152.67.210.39:9735`
- Electrum: `electrs.ever-chain.xyz:50001`
- 체인 hash: `6fe28c0ab6f1b372c1a6a246ae63f74f931e8365e15a089c68d6190000000000`
- swap-in xpub: BitEver 전용
- chainHash 강제 + chainParams 매핑

## 빌드

```bash
# 먼저 lightning-kmp publish 필수 (위 의존 참고)
cd /root/md/phoenix
./gradlew :phoenix-android:assembleDebug
```

산출: `phoenix-android/build/outputs/apk/debug/phoenix-115-XXXX-mainnet-debug.apk`

폰 2대에 모두 설치하여 테스트.

## 사용 흐름 (5개 마일스톤별)

### 1. 자동 채널 생성

1. 앱 첫 실행 → wallet 생성/복구
2. Receive → "On-chain wallet" → swap-in 주소(`bc1p…`) 복사
3. 외부 BEC 지갑에서 송금 (예: 2,500,000 sat)
4. 3 conf 후 자동으로 채널 생성 ("Connecting..." → "Connected")

### 2. Bolt12 송금 (양측 채널 보유)

1. 폰 B: Receive → "Use a reusable Bolt 12 offer" → `lno1…` 복사
2. 폰 A: Send → `lno1…` 입력 → 금액 입력 → Send

### 3. Mutual Close

Settings → Channels → 채널 선택 → "Mutual close" → 회수 주소 입력 + feerate 선택 → 확인

### 4. Force Close

Settings → Channels → "Force close all channels" → 확인. commit tx 즉시 브로드캐스트, 144 블록 후 final wallet 자동 회수.

### 5. 채널 없는 폰에게 Bolt12 송금 (신규)

1. 폰 B: 앱 첫 실행 후 swap-in 없이 바로 Receive → "Use a reusable Bolt 12 offer" → `lno1…` 복사
2. 폰 A: Send → `lno1…` 입력 → **10,000 sat 이상** 입력 → Send
3. 폰 B에 자동 새 채널 생성 + 잔액 도착

⚠️ 1,000 sat 같이 작은 금액은 mining fee 미달로 거절됨 (`Payment rejected: amount too low`). 10,000 sat 이상 권장.

## 알려진 미해결 이슈

- 외부 L1으로 송금 실패: `Aborted by peer [previous tx missing from tx_add_input (serial_id=000000000000)]`
- 폰 A에서 Request Liquidity 항상 실패 (폰 B는 가끔 성공, 가끔 실패)
- 일부 지정 mutual close 시나리오에서 잔액은 빠지지만 close 항목 누락 + 채널 negotiating 정지

LN_5에서 해결 예정.

## 상세 가이드

전체 재구현 가이드: `260517_LN_4.md` (별도 위치).
