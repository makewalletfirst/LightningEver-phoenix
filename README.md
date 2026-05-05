# LightningEver-bitever-phoenix (Mobile Wallet)

BitEver (BEC) L1 체인 전용 라이트닝 지갑 앱입니다. 
ACINQ Phoenix Wallet을 기반으로 비트에버 에코시스템에 맞게 커스터마이징되었습니다.

## 🚀 주요 커스텀 사항
- **Branding:** 앱 이름 `LightningEver`로 변경, 티커 `BEC` (`ever`) 적용.
- **LSP Integration:** `gemini` 서버의 Eclair LSP로 기본 연결 설정.
- **Liquidity Policy Relaxation:** 채널 개설 시 수수료 정책을 완화하여 비트에버 체인 초기 유동성 공급이 원활하도록 개선.
- **Header Pass-through:** `bitever-eclair-kmp` 라이브러리를 통해 비트에버 블록 검증 문제 해결.

## 🛠 기술 스택
- **언어:** Kotlin (Android), Swift (iOS - Shared logic in Kotlin)
- **프레임워크:** Jetpack Compose (Android), Kotlin Multiplatform (Shared)
- **빌드 도구:** Gradle (Kotlin DSL)

## 🏗 빌드 방법 (Android)
### 사전 요구사항
- Java 21 설치
- `bitever-eclair-kmp` 라이브러리가 로컬 Maven에 설치되어 있어야 함

### APK 빌드
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./gradlew :phoenix-android:assembleDebug -x allTests
```

## ⚙️ CI/CD (GitHub Actions)
`.github/workflows/android.yml` 파일을 통해 자동으로 Android APK 빌드를 테스트합니다.
