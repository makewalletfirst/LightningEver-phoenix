# LightningEver-bitever-phoenix (BitEver fork)

ACINQ Phoenix Android의 BitEver 체인 전용 패치 fork. 원본 README는 `README.md` 참고.

## 브랜치

- **`260517`** — 2026-05-17 안정판 (4가지 마일스톤 동작):
  1. 외부 L1 swap-in → 채널 자동 생성
  2. 폰 A → 폰 B Bolt12 송금 (LSP trampoline 1-hop)
  3. 앱에서 지정 주소 mutual close
  4. 앱에서 force close (commit tx 브로드캐스트, to_local 회수는 720 블록 CSV 후)

## 주요 변경점 (vs upstream)

| 파일 | 변경 사유 |
| --- | --- |
| `phoenix-shared/build.gradle.kts` | `-DincludeIos=false` 또는 `System.getProperty("includeIos")=false`일 때 iOS targets 빌드 skip. Linux/Android 전용 빌드 환경에서 iOS Cocoapods 의존성을 회피. |
| `phoenix-shared/src/commonMain/kotlin/fr.acinq.phoenix/data/lnurl/LnurlAuth.kt` | `Crypto.compact2der`가 bitcoin-kmp 신버전에서 제거됨 → `Secp256k1.compact2der`로 대체. |
| `phoenix-shared/src/commonMain/kotlin/fr.acinq.phoenix/utils/channels/SpendChannelAddressHelper.kt` | musig2 nonce 정렬 보강 (lightning-kmp의 Commitments.kt 패치와 같은 이유). `Scripts.sort()` 결과에 맞춰 `publicNonces` 정렬. |
| **lightning-kmp 의존성** | `mavenLocal()` 추가 + `1.11.5-DEBUG` 사용 (BitEver fork) |
| **Eclair 의존성** (전제) | `0292dd5f4379…` 노드와 `152.67.210.39:9735` LSP가 함께 동작 중이어야 함 |

추가로 동봉된 패치된 lightning-kmp 빌드가 `~/.m2/repository/fr/acinq/lightning/lightning-kmp-core/1.11.5-DEBUG/`에 있어야 빌드 가능.

상세 가이드: 후속에 동봉되는 `260517_LN_3.md` 참고.

## 빌드법

### 사전 조건
1. lightning-kmp(BitEver fork) 빌드 후 mavenLocal에 publish:
   ```bash
   cd /path/to/LightningEver-bitever-eclair-kmp
   git checkout 260517
   ./gradlew :lightning-kmp-core:publishToMavenLocal
   ```
2. JDK 17+, Android SDK, Gradle 8.x

### Android APK 빌드
```bash
git clone https://github.com/makewalletfirst/LightningEver-bitever-phoenix.git
cd LightningEver-bitever-phoenix
git checkout 260517

./gradlew :phoenix-android:assembleDebug -DincludeIos=false
# 산출물: phoenix-android/build/outputs/apk/debug/phoenix-115-3dd75162-mainnet-debug.apk
```

빌드 시간: 약 1~2분 (clean 빌드 시 5분 정도).

### 폰 설치
```bash
adb install -r phoenix-android/build/outputs/apk/debug/phoenix-115-3dd75162-mainnet-debug.apk
```

## 구동법

1. 앱 실행 → 새 지갑 생성 (또는 기존 mnemonic 복구)
2. LSP `0311fb42…@152.67.210.39:9735`로 자동 peer 연결
3. swap-in 화면에서 `bc1p…` 주소 표시 → 외부 L1으로 송금
4. ~3 confirmation 후 자동 채널 생성 (~52,500,000 sat capacity)
5. send 화면에서 Bolt12 offer (`lno1…`) 입력 → 금액 입력 → 송금

## 트러블슈팅

| 증상 | 해결 |
| --- | --- |
| 빌드 실패 `Could not find lightning-kmp-core:1.11.5-DEBUG` | lightning-kmp fork를 먼저 mavenLocal에 publish |
| 빌드 시 iOS 관련 에러 | `-DincludeIos=false` 인자 추가 |
| 앱 시작 후 채널이 안 보임 | LSP가 살아있고 같은 chainHash (BitEver) 사용 중인지 확인 |
| 외부 swap-in 후 채널이 abort | Eclair plugin JAR이 인자로 로드 안 됨 → `260517_LN_RESTORE.md` 참고 |
| force close 후 잔고 안 들어옴 | 정상. 720 블록 (to_self_delay) CSV 후 자동 회수 |

## 라이선스

GPL v3 (upstream Phoenix와 동일)
