# LightningEver-bitever-phoenix (SAVE Branch)

BitEver Lightning Network의 **LightningEver 앱** — Phoenix 기반 Android 라이트닝 지갑입니다.

## 개요
- `simple_taproot_phoenix` 채널 타입 지원
- Bolt12 결제 주소 생성 및 결제
- Eclair LSP 자동 연결 및 채널 자동 개설
- Musig2 2-of-2 taproot funding output 서명

## 커스터마이징 내역
| 항목 | 내용 |
|------|------|
| 체인 | BitEver 커스텀 체인 (regtest 기반) |
| LSP 주소 | 10.8.0.1:9735 (WireGuard VPN) |
| 채널 타입 | simple_taproot_phoenix |
| 최소 채널 금액 | 0.03 BEC |

## 빌드 방법
```bash
./gradlew :composeApp:assembleDebug
# APK: composeApp/build/outputs/apk/debug/
```

## 의존성
- `bitever-bitcoin-kmp`: 커스텀 bitcoin-kmp (LightningEver-bitever-eclair-kmp 레포)
- `lightning-kmp`: Lightning 프로토콜 구현

## 채널 플로우
```
1. 앱 설치 → Eclair LSP(10.8.0.1:9735) 자동 연결
2. L1 주소로 0.03 BEC 이상 입금
3. 자동 채널 개설 (WaitForChannelReady → NORMAL, 약 10분)
4. Bolt12 주소로 결제 가능
```

## 전체 구조
SAVEL_LN.md 참조 (LightningEver-bitever-eclair 레포).
