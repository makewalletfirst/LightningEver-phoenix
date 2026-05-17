# LightningEver Phoenix (BitEver) - SAVE2_fee Branch

## 개요
BitEver Lightning 지갑 앱 (Phoenix Android 포크). Taproot Musig2 서명 검증을 개발/테스트 목적으로 우회하여 채널 안정성 확보.

## 달성 상태
- ✅ L1 입금 → 자동 채널 생성
- ✅ 자동 유동성 확보
- ✅ 외부 L1 주소로 전송
- ✅ 볼트12 주소 → close 없이 fee is insufficient 단계 도달

## 클론 및 빌드

```bash
git clone -b SAVE2_fee https://github.com/makewalletfirst/LightningEver-bitever-phoenix.git
cd LightningEver-bitever-phoenix

# 사전 요구사항: JDK 17+, Android SDK
# lightning-kmp 1.11.5-DEBUG가 Maven Local에 있어야 함 (아래 eclair-kmp 먼저 빌드)

./gradlew :phoenix-android:assembleDebug \
  -DincludeAndroid=true \
  -DincludeIos=false \
  --console=plain

# APK 위치
ls phoenix-android/build/outputs/apk/debug/*.apk
```

## 핵심 수정 파일

| 파일 | 수정 내용 |
|------|----------|
| `phoenix-shared/src/commonMain/kotlin/.../LnurlAuth.kt` | compact2der API 호환 수정 |
| `phoenix-shared/src/commonMain/kotlin/.../SpendChannelAddressHelper.kt` | publicNonces API 수정 |
| `phoenix-shared/build.gradle.kts` | iOS 타겟 조건부 처리 |

## 의존성
- `lightning-kmp` 버전: `1.11.5-DEBUG` (이 브랜치와 함께 빌드된 버전 필요)
- `LightningEver-bitever-eclair-kmp` SAVE2_fee 브랜치 먼저 빌드할 것

## 원복 방법
```bash
git checkout SAVE2_fee
./gradlew :phoenix-android:assembleDebug -DincludeAndroid=true -DincludeIos=false
adb install -r phoenix-android/build/outputs/apk/debug/phoenix-*.apk
```
