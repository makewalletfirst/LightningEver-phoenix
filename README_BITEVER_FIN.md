# BitEver Phoenix (LightningEver) — 260517_FIN 브랜치 (FINAL)

LightningEver의 Android 앱 (사용자 UI + lightning-kmp 통합). ACINQ Phoenix Android를 BitEver L1 체인용으로 fork.

## 7가지 검증된 사용자 시나리오

1. ✅ **자동 채널 생성**: 외부 BEC 지갑에서 swap-in 주소로 송금 → 3 conf 후 자동 채널 활성화 + 50M sat 유동성
2. ✅ **양측 채널 보유 Bolt12 송금**: 일반적인 Lightning 송금
3. ✅ **채널 없는 폰에게 Bolt12 송금**: 수신자 새 wallet (잔액 0)이어도 Bolt12 offer만 받으면 OK — LSP가 자동으로 새 채널 만들어줌
4. ✅ **외부 L1 송금**: Send → bc1… 주소 입력 → splice-out으로 외부 L1에 전달
5. ✅ **지정 mutual close**: Settings → Channels → Mutual close → 회수 주소
6. ✅ **Force close**: Settings → Channels → Force close → 144 블록 후 자동 회수
7. ✅ **Request Liquidity (splice-in)**: Settings → Channels → Request liquidity → 채널 잔액 추가

## 의존성

- `lightning-kmp` 1.11.5-DEBUG (BitEver fork): `makewalletfirst/LightningEver-bitever-eclair-kmp` branch `260517_FIN`
- LSP: `makewalletfirst/LightningEver-bitever-eclair` branch `260517_FIN`

빌드 전 KMP를 `~/.m2`에 publish 필수.

## 빌드

```bash
# 1. 먼저 KMP publish (의존)
cd /path/to/lightning-kmp
./gradlew :lightning-kmp-core:publishToMavenLocal

# 2. Phoenix APK 빌드
cd /path/to/phoenix
./gradlew :phoenix-android:assembleDebug
```

산출: `phoenix-android/build/outputs/apk/debug/phoenix-115-XXXX-mainnet-debug.apk`

폰 2대에 동일 APK 설치 (테스트용 2폰 환경 권장).

## 앱 설정 (소스 코드 내장)

대부분 빌드 시 코드에 포함되므로 추가 설정 불필요:

| 항목 | 값 |
| --- | --- |
| 패키지명 | `fr.acinq.phoenix.testnet` (이름만 testnet, 실제 BitEver mainnet) |
| 앱 표시명 | LightningEver |
| LSP 노드 ID | `0311fb42898ef97d86e5b4e2615040edd91e1ca350cea6e573b7af080fba105e07` |
| LSP IP:포트 | `152.67.210.39:9735` |
| Electrum | `electrs.ever-chain.xyz:50001` (no TLS) |
| 체인 hash | `6fe28c0ab6f1b372c1a6a246ae63f74f931e8365e15a089c68d6190000000000` |
| swap-in xpub | BitEver 전용 master pubkey |

## 사용 흐름 (각 시나리오)

### 1. 자동 채널 생성 (신규 사용자)

1. 앱 첫 실행 → "Create new wallet" → 시드 12단어 저장 → PIN 설정
2. 자동으로 LSP 연결 + 폰 nodeId 생성
3. **(LSP 운영자에게 폰 nodeId 알려주기 → peer-whitelist 추가)** — 이 단계 없으면 50M auto-funding 안 됨
4. `Receive → On-chain wallet → swap-in 주소 (bc1p…)` 복사
5. 외부 BEC 지갑에서 송금 (예: 2,500,000 sat)
6. 3 conf 후 채널 자동 활성화 (UI에 "Connecting..." → "Ready" 표시)

### 2. Bolt12 송금 (양측 채널)

1. 수신측: `Receive → Use a reusable Bolt 12 offer → lno1…` 복사
2. 송금측: `Send → lno1…` 입력 → 금액 → `Send`
3. 즉시 송금 완료

### 3. 채널 없는 폰에게 Bolt12 송금 (OTF) ⭐ 신규

1. 수신측 (Phone B): 갓 설치, swap-in 없이도 `Receive → Use a reusable Bolt 12 offer → lno1…` 생성 가능
2. 송금측 (Phone A): **10,000 sat 이상** 입력 → Send
3. Phone B에 자동으로 새 채널 생성 + 잔액 도착
4. ⚠️ 1,000 sat 같이 작은 금액은 `Payment rejected: amount too low` (mining fee 미달)

### 4. 외부 L1 송금 (splice-out)

1. `Send → bc1... 외부 주소` 입력 → 금액 → `Send`
2. splice-out tx 자동 broadcast
3. 외부 주소 confirm 대기

### 5. Mutual Close (지정 주소)

1. `Settings → Channels → 채널 선택 → Mutual close`
2. 회수 주소 + feerate 입력 → `Confirm`
3. closing tx broadcast → confirm 후 지정 주소 입금

### 6. Force Close

1. `Settings → Channels → Force close all channels` → 확인
2. commit tx 즉시 broadcast → confirm
3. **144 블록 (~24h) 대기** → claim-main-delayed tx 자동 broadcast → final wallet 입금
4. UI에 "Complete"가 떴는데 잔액 없으면 CSV 대기 중 (정상)

### 7. Request Liquidity (splice-in)

1. `Settings → Channels → Request liquidity`
2. 금액 입력 (예: 50,000 sat)
3. splice tx broadcast → confirm 후 inbound capacity 증가

## 상세 가이드

- 아키텍처: `LightningEver_Architecture.md`
- 완전 재구현 가이드: `260517_LN_FIN.md`

## Phoenix mainnet 대비 차이 / 제약

| 기능 | Phoenix mainnet | LightningEver | 비고 |
| --- | --- | --- | --- |
| 외부 LN 송금 (다른 LSP 경유) | ✅ | ❌ | BEC LSP가 외부 LN 노드와 채널 없음 (폐쇄망) |
| 환율 / fiat 환산 | ✅ | ⚠️ 0 표시 | BEC 거래소 없음 |
| BIP-353 DNS 주소 | ✅ | ⚠️ 미사용 | BEC 도메인 없음 |
| Liquidity Ads 마켓 | ✅ (여러 LSP) | ❌ | 단일 LSP |
| Force-close CSV 대기 | 2016 블록 | 144 블록 | 단축 |
| Channel reserve | 1% | 0 | zero-reserve |
| MPP 송금 | ✅ | ✅ | 동일 |
| Trampoline 송금 | ✅ | ✅ | 동일 |
| Bolt12 offer | ✅ | ✅ | 동일 |
| 자동 채널 생성 | ✅ | ✅ | 동일 |
| OTF 채널 (수신측) | ✅ | ✅ | 동일 |
| FCM wake-up | ✅ | ✅ | 동일 |
| simple_taproot_channels | ✅ | ✅ | phoenix_simple_taproot_channel |
| Cloud backup | ✅ | ⚠️ 미테스트 | 코드는 있음 |
| LNURL pay/withdraw | ✅ | ⚠️ 미테스트 | 외부 서비스 없음 |
| Tor 지원 | ✅ | ⚠️ 미사용 | onion 인프라 없음 |
| Watchtower | ❌ | ❌ | Phoenix는 원래 안 함 |

**기능 매트릭스 위주로는 mainnet Phoenix와 거의 동등**. 누락된 항목들은 LightningEver 코드의 문제가 아니라 BitEver 생태계 자체에 외부 LN/거래소/도메인 인프라가 아직 없기 때문.

## 보안 경고

이 빌드는 LSP 측 musig2/HTLC 서명 검증 BYPASS, channel reserve BYPASS 등을 전제로 동작합니다. 실제 비트코인 mainnet과 호환 안 됩니다. BitEver test chain 전용.
