# LightningEver-bitever-phoenix (Mobile Wallet)

BitEver(BEC) L1 체인 전용 라이트닝 지갑 앱. ACINQ Phoenix Wallet을 기반으로 비트에버 에코시스템에 맞게 커스터마이징됨.

## 🧭 이 앱의 위치

```
[Eclair LSP (Scala, 서버)] ←━━━ Lightning P2P ━━━→ [LightningEver-bitever-phoenix (이 repo)]
                                                            │
                                                            └─ depends on ─→ [LightningEver-bitever-eclair-kmp]
```

- **이 repo (Phoenix 포크)** — UI/네트워킹/DB 셸. 안드로이드/iOS 사용자 대면 앱.
- **eclair-kmp** — 단말 안에서 실제 라이트닝 노드를 구동하는 코어 라이브러리 (`mavenLocal()` 의존).
- **eclair (Scala LSP)** — 외부 서버 측 라이트닝 노드. P2P (`9735`) 로 통신.

## 🚀 주요 커스텀 사항 (이전 작업분 누적)

- **Branding:** 앱 이름 `LightningEver`, 티커 `BEC` (`ever`).
- **LSP Integration:** BitEver Eclair LSP 로 기본 연결 설정.
- **Liquidity Policy Relaxation:** 채널 개설 수수료 정책 완화로 BEC 초기 유동성 공급 원활화.
- **Header Pass-through:** `bitever-eclair-kmp` 의존성으로 BEC 블록 검증 문제 해결.

## 🆕 260507 브랜치 — 이전 (`260505`/main 누적분) 대비 변화

### 1. lightning-kmp 의존성 버전 변경
파일: `gradle/libs.versions.toml`

```toml
[versions]
lightningkmp = "1.11.5-DEBUG"   # 이전: "1.11.5"
```

`mavenLocal()` 가 이미 dependency repository 에 첫 번째로 등록되어 있어 별도 설정 변경 없이 자동 인식.

### 2. 왜 이 변화가 필요했는가
`LightningEver-bitever-eclair-kmp` 의 `260507` 브랜치에 추가된 close handler 진단 logger 를 앱이 가져갈 수 있도록 버전 동기화. 진단 logger 는 앱의 **Settings → Troubleshooting → View Logs** 에서 `[260507-DBG]` 키워드로 검색 가능.

### 3. 검증된 동작
- `260506` 까지 미해결이었던 **앱 close/force-close 버튼 무동작** 의 silent fail 위치를 추적할 수 있는 진단 빌드.
- LSP `/close` 가 보낸 `Shutdown(with ShutdownNonce TLV)` 에 앱이 반응하는지, 어떤 분기로 빠지는지 검증.
- 새로 빌드한 APK 로는 init handshake 가 일시적으로 timeout 될 수 있으므로 설치 후 **앱 강제 종료 + 재실행** 권장.

## 🛠 기술 스택

- **언어:** Kotlin (Android), Swift (iOS — shared 로직은 Kotlin Multiplatform).
- **프레임워크:** Jetpack Compose (Android), Kotlin Multiplatform (`phoenix-shared`).
- **빌드 도구:** Gradle 8.x + Kotlin 2.x.
- **Java:** OpenJDK 21.

## 🏗 빌드 방법

### 사전 요구사항
- Java 21 (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`).
- **`bitever-eclair-kmp` 라이브러리가 mavenLocal 에 publish 되어 있어야 함**:
  ```bash
  cd /path/to/LightningEver-bitever-eclair-kmp
  ./gradlew :lightning-kmp-core:publishJvmPublicationToMavenLocal \
             :lightning-kmp-core:publishKotlinMultiplatformPublicationToMavenLocal
  # 결과: ~/.m2/repository/fr/acinq/lightning/lightning-kmp-core/1.11.5-DEBUG/
  ```

### Android APK 빌드 (debug)
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
./gradlew :phoenix-android:assembleDebug --console=plain
# ~2분 39초
```

결과 APK: `phoenix-android/build/outputs/apk/debug/phoenix-115-<GIT_HASH>-mainnet-debug.apk` (~38 MB, debug-signed).

### Android APK 빌드 (release)
```bash
./gradlew :phoenix-android:assembleRelease
```
release 는 `signingConfigs` 설정 필요.

### iOS 빌드
별도의 Mac 환경 필요. `phoenix-ios/` 디렉토리에서 Xcode 프로젝트로 빌드.

## 📲 단말 설치

### USB 연결 + adb 사용
```bash
adb install -r phoenix-android/build/outputs/apk/debug/phoenix-115-*-mainnet-debug.apk
# -r : 기존 앱 데이터 보존 (서명 동일 시)
```

### SCP 로 전송 후 단말에서 sideload
1. 빌드된 APK 를 사용자 단말로 전송 (SCP, USB, 클라우드, 등).
2. 단말의 파일 매니저에서 APK 실행 → "알 수 없는 출처 허용" → 설치.
3. **앱 강제 종료 + 재실행** 권장 (init handshake 정상화).

## 📖 사용 흐름

1. 앱 실행 → 자동으로 LSP에 P2P 연결 시도 (`init` handshake).
2. **수동 채널 개설** 은 LSP 측 `/open` API 로 진행 (LSP repo `260507` README 참조).
3. **유동성 주입** 은 amountless 인보이스 발급 + LSP `/payinvoice` 로 결제.
4. **send (splice-out)** = 메인 화면의 Send 버튼 → 외부 L1 주소 입력 → 송금.
5. **close / force-close** = Settings → Channel Management → 해당 채널 → close 버튼.
6. **로그 확인** = Settings → Troubleshooting → View Logs (`[260507-DBG]` 검색).

## 🌿 브랜치 정책
- `master` / `main` (있다면) — upstream Phoenix 추적.
- `260505` — 초기 BEC 브랜딩 + LSP 연결 + Liquidity policy.
- **`260507`** — `lightningkmp = 1.11.5-DEBUG` 의존성 변경 (현 브랜치).

## 🔗 함께 보는 문서
- LSP 운영 + 전체 절차 통합 가이드: 운영 머신 `/root/md/260507_CLOSECOMPLETE.md`.
- LSP 측 패치 / REST API: `LightningEver-bitever-eclair` repo `260507` 브랜치 README.
- 라이트닝 라이브러리 내부 패치: `LightningEver-bitever-eclair-kmp` repo `260507` 브랜치 README.

## ⚠️ 주의

- **테스트 체인 전용 빌드**. mainnet 운영 시 `1.11.5-DEBUG` 의 진단 logger 가 sensitive 정보를 로그 파일에 남길 수 있음.
- 새 APK 설치 시 `keep-alive`/`reconnect` 단절 이슈로 init timeout 이 한 번 발생할 수 있음. **앱 강제 종료 후 재실행** 으로 해결.

## ⚙️ CI/CD (GitHub Actions)
`.github/workflows/android.yml` 파일을 통해 자동으로 Android APK 빌드를 테스트합니다.
