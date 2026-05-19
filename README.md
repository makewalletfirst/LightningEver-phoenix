# LightningEver-bitever-phoenix

BitEver L1 위에서 동작하는 **LightningEver 앱** — ACINQ Phoenix를 BitEver 체인 + 자체 LSP에 맞춰 fork한 Android 라이트닝 지갑입니다.

브랜치: `main` (운영) / `260517_FIN` (스냅샷)

---

## 검증된 기능 (7가지 마일스톤)

1. **L1 swap-in → 자동 채널 생성** — 외부 BEC 지갑에서 swap-in 주소로 송금 → 3 conf 후 자동 채널 활성화 + ~50M sat inbound liquidity 자동 공급
2. **Bolt12 송금 (양측 채널 보유)** — 일반 lightning 결제, trampoline 라우팅
3. **Bolt12 송금 (채널 없는 수신자)** — 수신자가 wallet 갓 설치 + 잔액 0이어도 OK, LSP가 OTF로 새 채널 자동 생성
4. **외부 L1 송금 (splice-out)** — Send → bc1... 주소 입력 → splice tx로 직접 외부 L1에 전달
5. **지정 mutual close** — Settings → Channels → Mutual close, 회수 주소 + feerate 지정
6. **Force close** — commit tx 즉시 broadcast, 144 블록 CSV 후 자동 회수
7. **Request Liquidity (splice-in)** — 채널에 inbound 추가 (channel reserve BYPASS 적용)

---

## 직접 건드린 부분 (Phoenix 앱 내부)

### 1. 앱 아이덴티티 / 브랜딩

| 항목 | 값 |
| --- | --- |
| 앱 표시명 | **LightningEver** |
| 패키지명 | `fr.acinq.phoenix.testnet` (이름만 testnet, 실제는 BitEver mainnet) |
| 앱 아이콘 | LE_logo (5 dpi tier × 3 variants = 15개 PNG) + adaptive icon (foreground inner 50% padding) |
| 아이콘 배경색 | `#FFFFFF` (ACINQ 보라색 → 흰색) |

### 2. 체인 / 네트워크 설정 (소스 코드 하드코딩)

| 항목 | 값 | 위치 |
| --- | --- | --- |
| chainHash | `6fe28c0ab6f1b372c1a6a246ae63f74f931e8365e15a089c68d6190000000000` (BitEver mainnet) | `phoenix-shared/.../managers/NodeParamsManager.kt` |
| LSP nodeId | `0311fb42898ef97d86e5b4e2615040edd91e1ca350cea6e573b7af080fba105e07` | 동일 |
| LSP 주소 | `152.67.210.39:9735` (공개 IP, VPN 경유 가능) | 동일 |
| Electrum | `electrs.ever-chain.xyz:50001` (TLS off) | `phoenix-shared/.../data/ElectrumServers.kt` |
| 블록 탐색기 | `https://bitever2.ever-chain.xyz` | `phoenix-shared/.../utils/BlockchainExplorer.kt` |
| Fee API | `https://bitever2.ever-chain.xyz/api/v1/fees/recommended` | `phoenix-shared/.../managers/global/FeerateManager.kt` |
| swap-in xpub | BitEver 전용 master pubkey | NodeParamsManager.kt |
| 채널 타입 | `simple_taproot_phoenix` (musig2 2-of-2) | KMP `NodeParams.kt` |
| toRemoteDelayBlocks | `144` (기본 2016 → 단축) | KMP `NodeParams.kt:259` |

### 3. lightning-kmp 측 동반 패치 (Phoenix가 의존)

7개 musig2 nonce ordering 패치 + feerate fallback + Negotiating 상태 복원 등.

자세한 내용: [makewalletfirst/LightningEver-bitever-eclair-kmp](https://github.com/makewalletfirst/LightningEver-bitever-eclair-kmp) 의 `README_BITEVER_FIN.md` 참고.

---

## 의존성 (간단)

- **`lightning-kmp` 1.11.5-DEBUG**: [LightningEver-bitever-eclair-kmp](https://github.com/makewalletfirst/LightningEver-bitever-eclair-kmp) branch `260517_FIN` (또는 `main`)
  - 빌드 전 `./gradlew :lightning-kmp-core:publishToMavenLocal` 로 `~/.m2/repository/`에 publish 필수
- **Eclair LSP**: [LightningEver-bitever-eclair](https://github.com/makewalletfirst/LightningEver-bitever-eclair) (실행 인프라, 앱 빌드와 무관)

---

## 빌드 방법

```bash
# 1. KMP 먼저 publish (의존 라이브러리)
cd /path/to/lightning-kmp
./gradlew :lightning-kmp-core:publishToMavenLocal
# ~3분, ~/.m2/repository/fr/acinq/lightning/lightning-kmp-core-jvm/1.11.5-DEBUG/ 산출

# 2. Phoenix Android APK 빌드
cd /path/to/phoenix
./gradlew :phoenix-android:assembleDebug
# 첫 빌드 ~10분, 증분 ~30초
```

산출물:
```
phoenix-android/build/outputs/apk/debug/phoenix-115-<HASH>-mainnet-debug.apk
```

> Note: 모듈명은 `phoenix-android` (구버전 README의 `composeApp` 아님). Compose Multiplatform 마이그레이션 이전 구조.

---

## CI

`.github/workflows/android.yml`에서 GitHub Actions가:
1. lightning-kmp fork repo 클론
2. `:lightning-kmp-core:publishToMavenLocal` 실행
3. `:phoenix-android:assembleDebug` 실행

main 브랜치에 push 시 자동 빌드 검증.

---

## 사용 흐름 (간단)

### 신규 사용자

```
앱 설치 → Create new wallet → 시드 12단어 저장 → PIN 설정
       → 자동으로 LSP(152.67.210.39:9735)에 peer 연결
       → 폰 nodeId 생성됨
       → (LSP 운영자에게 nodeId 알려서 peer-whitelist 등록)
       → Receive → On-chain wallet → swap-in 주소(bc1p…) 복사
       → 외부 BEC 지갑에서 송금 (예: 2,500,000 sat)
       → 3 conf 후 자동 채널 활성화 (~52M sat capacity)
```

### Bolt12 송금 (양측 채널 보유)

```
수신자: Receive → Use a reusable Bolt 12 offer → lno1… 복사
송신자: Send → lno1… 입력 → 금액 → Send
```

### Bolt12 송금 (수신자 채널 없음)

```
수신자: 갓 설치, swap-in 없이도 lno1… offer 생성 가능
송신자: Send → lno1… 입력 → 10,000 sat 이상 → Send
        → LSP가 OTF로 수신자에게 새 채널 자동 생성 → 잔액 도착
```

⚠️ 1,000 sat 같이 작은 금액은 mining fee 미달로 거절됨. **10,000 sat 이상** 권장.

### 외부 L1 송금

```
Send → bc1... 외부 주소 → 금액 → Send (splice-out tx broadcast)
```

### 채널 정리

```
Settings → Channels → 채널 선택
  ├─ Mutual close → 회수 주소 + feerate (정상 종료, 즉시 회수)
  └─ Force close   → 144 블록 후 자동 회수 (UI에 Complete 떠도 CSV 대기 정상)
```

### Request Liquidity (inbound 추가)

```
Settings → Channels → Request liquidity → 금액 입력 → 확인
splice tx confirm 후 inbound capacity 증가
```

---

## Phoenix mainnet 대비 기능 매트릭스

| 기능 | Phoenix mainnet | LightningEver | 비고 |
| --- | --- | --- | --- |
| 자동 채널 생성 | ✅ | ✅ | 동일 |
| Bolt12 send/receive | ✅ | ✅ | 동일 |
| Bolt12 OTF (채널 없는 수신자) | ✅ | ✅ | 동일 |
| MPP / Trampoline | ✅ | ✅ | 동일 |
| Splice-in/out | ✅ | ✅ | 동일 |
| Mutual / Force close | ✅ | ✅ | force-close CSV 단축 (2016 → 144) |
| Channel reserve | 1% | 0 (BYPASS) | zero-reserve |
| FCM wake-up | ✅ | ✅ | 동일 |
| simple_taproot_channels | ✅ | ✅ | `phoenix_simple_taproot_channel` 변종 |
| 외부 LN 송금 (다른 LSP 경유) | ✅ | ❌ | BEC LSP는 외부 LN과 채널 없음 |
| 환율 / fiat 표시 | ✅ | ⚠️ 0 표시 | BEC가 거래소에 없음 |
| BIP-353 DNS 주소 | ✅ | ⚠️ 미사용 | BEC 도메인 인프라 없음 |
| Liquidity Ads 마켓플레이스 | ✅ (여러 LSP) | ❌ | 단일 LSP |

핵심 LN 기능은 거의 동등. 미지원 항목은 Phoenix 코드 한계가 아니라 BitEver 생태계 자체에 외부 LN/거래소/도메인 인프라가 없기 때문.

---

## 보안 경고

이 빌드는 **BitEver 테스트 체인 전용**입니다. LSP 측에 다음 BYPASS가 적용되어 있어 비트코인 mainnet에서는 절대 사용 금지:
- musig2 partial signature 검증 BYPASS (Eclair core)
- HTLC signature 검증 BYPASS
- channel reserve 검사 BYPASS

서명 알고리즘 자체는 비트코인과 호환 (secp256k1 + schnorr + musig2). on-chain tx의 서명은 모두 valid (BitEver bitcoind가 정상 검증해서 수락). BYPASS는 LSP가 P2P 메시지에서 받는 서명을 사전 검증하지 않는 것일 뿐, 서명 생성은 정상.

자세한 분석: [LightningEver-bitever-eclair](https://github.com/makewalletfirst/LightningEver-bitever-eclair) repo 별도 문서.

---

## 라이선스

ACINQ Phoenix 기반 fork. 원본 라이선스 (Apache 2.0) 승계.
