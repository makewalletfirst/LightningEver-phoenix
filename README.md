# LightningEver-phoenix (BitEver Mobile Wallet)

Welcome to the **LightningEver** mobile wallet repository. This project is a specialized fork of ACINQ's **Phoenix Android** wallet, customized to run on top of the custom Layer 1 blockchain, **BitEver (BEC)**, utilizing a tailored **Eclair LSP** architecture.

---

## Table of Contents
1. [Overview](#1-overview)
2. [Ecosystem & Architecture](#2-ecosystem--architecture)
3. [Key Technology Stack](#3-key-technology-stack)
4. [Structure & Eclair's Role](#4-structure--eclairs-role)
5. [Main Modifications & Branch Milestones](#5-main-modifications--branch-milestones)
6. [Security Warnings & Dev Bypasses](#6-security-warnings--dev-bypasses)
7. [Build Instructions](#7-build-instructions)
8. [Deployment & Operations Guide](#8-deployment--operations-guide)
9. [Feature Matrix vs. Phoenix Mainnet](#9-feature-matrix-vs-phoenix-mainnet)

---

## 1. Overview

**LightningEver** is a complete, private, and localized Lightning Network implementation built for the **BitEver (BEC) L1 chain**. The core of this project is achieved by forking, customizing, and patching the ACINQ Phoenix ecosystem (specifically **Phoenix Android**, **lightning-kmp**, and **Eclair LSP**).

All mobile transactions are routed through a single **Liquidity Service Provider (LSP)** powered by a BitEver-patched **Eclair** daemon. Mobile clients utilize a zero-reserve, fast-closing, and auto-funding mechanism designed to provide a seamless user experience even on a young custom blockchain environment.

---

## 2. Ecosystem & Architecture

The following diagram illustrates the interaction between the different components of the LightningEver ecosystem:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              BitEver (BEC) L1                               │
│  ─────────────────────────────────────────────────────────────────────────  │
│  • Custom Bitcoin fork (Taproot, MuSig2, P2TR, custom chainHash)            │
│  • Independent mining and blockchain consensus                              │
│  • bitcoind RPC: 10.8.0.6:8334, ZMQ: 28332/28333                            │
│  • Electrs indexer: electrs.ever-chain.xyz:50001                            │
│  • chainHash: 6fe28c0ab6f1b372c1a6a246ae63f74f931e8365e15a089c68d6190000... │
└─────────────────────────────────────────────────────────────────────────────┘
          ▲                              ▲                          ▲
          │ RPC + ZMQ                    │ Electrum RPC             │ Electrum
          │                              │                          │
┌─────────┴───────────────┐  ┌───────────┴────────────┐  ┌──────────┴─────────┐
│   Eclair LSP            │  │  Phoenix Android       │  │  Phoenix Android   │
│   (BitEver fork)        │  │  = LightningEver App   │  │  = LightningEver   │
│   ─────────────────     │◀─┤  (Phone A)             │  │  (Phone B)         │
│   • node-id:            │  │  ─────────────────     │  │  ─────────────────│
│     0311fb42898e...     │  │  • uses lightning-kmp  │  │  • lightning-kmp   │
│   • IP: 152.67.210.39   │  │  • LSP peer 0311fb...  │  │  • LSP peer 03...  │
│   • Port: 9735 (P2P)    │  │                        │  │                    │
│   • Port: 8080 (REST)   │  │                        │  │                    │
│   • + channel-funding   │  │                        │  │                    │
│     plugin              │  │                        │  │                    │
│   • SQLite DB           │  │                        │  │                    │
└─────────────────────────┘  └────────────────────────┘  └────────────────────┘
          │                              ▲                          ▲
          │       Lightning P2P          │                          │
          ├──────────────────────────────┘                          │
          │       Lightning P2P                                     │
          └─────────────────────────────────────────────────────────┘
```

### Core Architecture Facts
- **No direct peer-to-peer mobile channels**: All payment and routing traffic between wallets passes through the central LSP (`0311fb42...`).
- **External Dependencies**: 
  - **Eclair LSP** relies on both **bitcoind JSON-RPC** (for block mining alerts, UTXO tracking, transaction broadcasts, and fee estimation) and **Electrs** (as a watcher interface).
  - **Phoenix Android** relies **solely on Electrs** (ElectrumX protocol) for L1 tracking, swap-in scans, and fee estimations. It never communicates directly with `bitcoind`.

---

## 3. Key Technology Stack

- **Mobile Wallet (this repository)**: Built using **Kotlin Multiplatform (KMP)** and **Jetpack Compose** for Android.
- **Lightning Protocol Engine**: **lightning-kmp** (Kotlin Multiplatform core library by ACINQ, forked and customized).
- **LSP Daemon**: **Eclair LSP** (Scala / JVM, customized for BEC L1 compatibility).
- **Local Databases**: SQLite database on Eclair LSP; **SQLDelight** on Android (`phoenix.db`).
- **External Interfaces**: JSON-RPC over HTTP (`bitcoind`), ElectrumX protocol (`Electrs`), TCP (BOLT 8 Noise XK transport for P2P network).
- **Push Services**: Google **Firebase Cloud Messaging (FCM)** for background wake-ups.

---

## 4. Structure & Eclair's Role

### Component Structure
1. **BitEver L1 (External)**: Custom blockchain base.
2. **Eclair LSP (JVM)**: Serves as the backbone routing node. Configured via `eclair.conf` and stores channel/payment data in `eclair.sqlite`. Includes a customized `channel-funding-plugin-0.13.1.jar` to auto-provision inbound liquidity.
3. **lightning-kmp (Maven core-jvm package)**: Embedded within the Android app to execute the state machine for channel management, splicing, payments, and Bolt12 invoice creation.
4. **Phoenix Android (Mobile client)**: The user interface layer, background services (`FCMService`, `PaymentsForegroundService`), and persistent storage.

### The Role of Eclair LSP
Eclair LSP acts as the single point of entry, trampoline router, and liquidity provider for all clients:
- **Automatic Channel Provisioning**: Automatically funds new channels (with ~50M sat inbound capacity) upon the user's initial swap-in deposit.
- **On-The-Fly (OTF) Channel Creation**: When a zero-balance user generates a Bolt12 offer, Eclair LSP detects incoming HTLCs, pauses relay, triggers a channel-open protocol with the receiver, and forwards the HTLC immediately into the newly established channel.
- **Background Wake-up**: Employs FCM push notifications to wake clients up during incoming payments when they are backgrounded or offline, securing successful asynchronous deliveries.

---

## 5. Main Modifications & Branch Milestones

### 7 Core Milestones (Verified Features)
1. **L1 Swap-in to Auto-Channel Creation**: Depositing BEC to the wallet's swap-in address triggers automatic dual-funded channel open with ~50M sat inbound liquidity from the LSP after 3 confirmations.
2. **Bolt12 Transacting (Both sides active)**: Allows standard Lightning payments with trampoline relay and Bolt12 offers.
3. **Bolt12 Transacting (Receiver has no channels)**: The LSP uses OTF (On-The-Fly) funding to dynamically create channels for zero-balance, newly installed wallets receiving Bolt12 payments (> 10,000 sat).
4. **External L1 Splice-out**: Send to a standard on-chain address directly from your active Lightning channel using a splice transaction.
5. **Custom Mutual Close**: Users can gracefully close their channel and extract funds directly to a specified L1 address with a custom fee rate (`Settings -> Channels -> Mutual Close`).
6. **Force Close**: Broadcasts the local commitment transaction immediately. The relative locktime (CSV) is shortened to `144` blocks (~24 hours) instead of the default `2016` blocks for faster L1 fund recovery.
7. **Request Liquidity (Splice-in)**: Add inbound capacity dynamically via channel splice. Built-in channel reserve bypass allows zero-reserve operations.

### Branch Highlights & Background Updates

#### `260521OFFBOLT12` — Offline Bolt12 Reception via FCM
Allows users to receive Bolt12 payments even when the receiving phone is locked or the app is killed.
- **App Side**: Configured with a dedicated `google-services.json` tied to the Firebase project `lightningever` under `fr.acinq.phoenix.lightningever` application ID.
- **KMP Side**: Fixed a `Negotiating` state lock by adding an unknown closing transaction fallback to ensure graceful recovery.
- **LSP Side**: Uses `PeerReadyNotifier` and `fcm-push-plugin` to send a high-priority FCM wakeup push to the recipient node ID, triggering automatic peer connection and channel synchronization in the background.

#### `260522_OFFSWAPIN` — Offline Swap-in Deposit Detection
Triggers automatic channel creation for offline users who have received on-chain funds on their swap-in address.
- **App Side**: Implemented `FCMService.kt` handler for the low-priority `"SwapInDeposit"` notification reason.
- **Background Service**: Integrates `PaymentsForegroundService.kt` to securely handle decryption fallbacks and start the foreground service, prompting `lightning-kmp` to query Electrs, detect the UTXO, and initiate the `OpenDualFundedChannel` flow.

---

## 6. Security Warnings & Dev Bypasses

> [!WARNING]
> This repository is built strictly for the **BitEver Development/Test chains**. Under no circumstances should this fork be compiled or used on the Bitcoin Mainnet.

To achieve continuous local testing and fast iteration on the BitEver chain, several cryptographic and consensus checks have been bypassed on the Eclair LSP and client sides:
- **`checkRemoteSig` / `checkRemotePartialSignature` / `checkRemoteHtlcSig` (Eclair LSP)**: Bypassed to always return `true` to ease integration testing of MuSig2/Schnorr channels.
- **Channel Reserve Bypass**: Eclair LSP bypasses the 1% channel reserve rule (`validateTx` in `InteractiveTxBuilder.scala`), allowing clients to spend up to their last satoshi (zero-reserve).
- **`validateParamsDualFundedNonInitiator`**: Bypasses the minimum funding check (`minFunding=0`) for OTF channels, enabling zero-balance mobile clients to accept new incoming payments.
- **MuSig2 Commit Nonces Bypass (`checkCommitNonces`)**: Ignores `MissingCommitNonce` errors for Taproot channels to prevent forced closures during peer reconnects.
- **BitEver Node Sync Bypass**: Bypasses Eclair's initial block download and synchronization checks (`Setup.scala`), as the BitEver L1 network has low cumulative work.
- **Feerate Fallback**: If the custom `estimatefee` RPC fails due to low block height, the system falls back to `MinimumFeeratePerKw` to avoid client initialization failures.

---

## 7. Build Instructions

To compile the application successfully, you must compile and publish the custom `lightning-kmp` fork first.

### Step 1: Build & Publish Custom `lightning-kmp`
1. Clone the custom KMP library:
   ```bash
   git clone https://github.com/makewalletfirst/LightningEver-bitever-eclair-kmp.git
   cd LightningEver-bitever-eclair-kmp
   ```
2. Publish it to your local Maven repository:
   ```bash
   ./gradlew :lightning-kmp-core:publishToMavenLocal
   ```
   *This outputs `lightning-kmp-core-jvm:1.11.5-DEBUG` to `~/.m2/repository/fr/acinq/lightning/`.*

### Step 2: Build Phoenix Android
1. Navigate back to this repository root:
   ```bash
   cd /root/md/phoenix
   ```
2. Assemble the debug APK:
   ```bash
   ./gradlew :phoenix-android:assembleDebug
   ```
3. Locate the completed APK:
   ```bash
   phoenix-android/build/outputs/apk/debug/phoenix-115-<HASH>-mainnet-debug.apk
   ```

---

## 8. Deployment & Operations Guide

### LSP Operation and Status Commands
To query, monitor, and run the Eclair LSP node, use the following REST API endpoints (authenticated via basic auth, `-u :PASSWORD`):

```bash
# 1. Get Node Info and Sync State
curl -s -u :PASSWORD -X POST http://localhost:8080/getinfo | jq .

# 2. List All Active Channels and States
curl -s -u :PASSWORD -X POST http://localhost:8080/channels | jq '.[] | {channelId, state, nodeId}'

# 3. View Details of a Specific Channel
curl -s -u :PASSWORD -X POST http://localhost:8080/channel -d "channelId=YOUR_CHANNEL_ID_HEX" | jq .

# 4. List Connected Peer Nodes
curl -s -u :PASSWORD -X POST http://localhost:8080/peers | jq .

# 5. Enable On-The-Fly Funding (Required once after restarting the LSP)
curl -s -u :PASSWORD -X POST http://localhost:8080/enablefromfuturehtlc

# 6. Stream Live LSP Logs
tail -f /root/.eclair/eclair.log
```

---

## 9. Feature Matrix vs. Phoenix Mainnet

| Feature | Phoenix Mainnet | LightningEver (BitEver) | Notes |
| :--- | :---: | :---: | :--- |
| **Auto Channel Opening** | ✅ | ✅ | Works identically. |
| **Bolt12 Send & Receive** | ✅ | ✅ | Tested and verified using Bolt12 offers. |
| **OTF Channel Funding** | ✅ | ✅ | Supported for payments above 10,000 sat. |
| **MPP / Trampoline Routing** | ✅ | ✅ | Native support via lightning-kmp. |
| **Splice-in / Splice-out** | ✅ | ✅ | Supported. |
| **Mutual / Force Close** | ✅ | ✅ | Force-close CSV reduced from 2016 to 144 blocks. |
| **Channel Reserve Requirement** | 1% | **0% (Bypassed)** | Zero-reserve enabled for smooth UX. |
| **FCM Background Wake-up** | ✅ | ✅ | Customized via specialized Firebase project. |
| **Simple Taproot Channels** | ✅ | ✅ | Integrated using the custom taproot scheme. |
| **External LN Routing** | ✅ | ❌ | Closed network. LSP has no external routing peers. |
| **Fiat Exchange Display** | ✅ | ⚠️ *0.00* | BitEver is not listed on external exchanges. |
| **BIP-353 DNS Addresses** | ✅ | ⚠️ *Disabled* | Requires BitEver domain lookup infrastructure. |
| **Liquidity Ads Market** | ✅ | ❌ | Single LSP architecture only. |

---

## License

This repository is a fork of ACINQ Phoenix Android and inherits its original **Apache License 2.0**.
