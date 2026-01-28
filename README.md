# 🧩 MyRythm  
**Android · Backend · IoT 통합 시스템**

MyRythm은 복약 관리와 건강 모니터링을 목표로 한  
**Android 앱 – Backend 서버 – IoT 디바이스가 유기적으로 연동되는 시스템**입니다.

이 프로젝트는 단일 플랫폼 구현이 아니라,  
각 구성 요소가 **명확한 역할과 책임을 갖고 협력하도록 설계된 통합 아키텍처**를 중심으로 구성되었습니다.

---

## 🔍 System Overview  
┌────────────┐ REST / JWT ┌────────────┐  
│ Android │ ───────────────────────▶ │ Backend │  
│ App │                               │ Server │  
│ │ ◀─────────────────────── │         │  
└─────▲──────┘ JSON / FCM  └─────▲──────┘  
      │                          │  
      │                 QR / BLE │ HTTP  
      │                          │  
┌─────┴──────┐Header Auth┌───────┴───────┐  
│ IoT │ ───────────────────────▶ │ Ingest / Cmd │  
│ Device │                        │ Endpoints │  
└────────────┘            └───────────────┘    



### Components & Roles
- **Android App**
  - 사용자 인터페이스 제공
  - 사용자 입력 및 트리거 처리
  - QR/BLE 기반 디바이스 등록
  - 서버 데이터 시각화

- **Backend Server**
  - 시스템의 중앙 판단자(Control Plane)
  - 인증/권한 관리
  - 복약 상태 판정
  - 디바이스 명령 분배
  - 데이터 영속화

- **IoT Device**
  - 센서 이벤트 생성
  - 서버 명령 수신
  - 물리적 알림 제공 (LED / Slot)

---

## 🎯 Core Design Goals

### 1. 역할이 분리된 시스템
각 구성 요소는 자신의 책임만 수행하며,  
판단과 상태 관리는 서버에 집중됩니다.

- 디바이스는 판단하지 않음
- 앱은 비즈니스 로직을 소유하지 않음
- 서버는 단일한 진실의 원천(Single Source of Truth)

---

### 2. 확장 가능한 구조
- Android: 멀티모듈 + Clean Architecture
- Backend: 도메인별 Django App 분리
- IoT: 하드웨어 차이를 흡수하는 펌웨어 구조

기능 추가 시 기존 구조를 깨지 않고 확장 가능하도록 설계되었습니다.

---

### 3. 실제 환경을 고려한 연동 흐름
- 네트워크 불안정
- 디바이스 재부팅
- 중복 이벤트
- 시간 오차

실제 사용 환경을 고려하여  
**서버 중심의 판정 구조**를 채택했습니다.

---

## 🔗 Key Integration Flow (Highlight)

### BLE · QR 기반 디바이스 등록 플로우

1. **Server**
   - 디바이스 UUID / TOKEN 생성
   - QR 코드 정보 제공

2. **Android App**
   - QR 스캔으로 UUID / TOKEN 획득
   - 로그인 사용자와 디바이스 연결 요청

3. **IoT Device**
   - BLE 등록 모드 진입
   - 앱으로부터 Wi-Fi 및 인증 정보 수신
   - 정상 모드로 전환

4. **Server**
   - 디바이스 인증 정보 검증
   - 이후 모든 통신은 Header 기반 인증 사용

---

## 📱 Android Application

### Characteristics
- Jetpack Compose 기반 UI
- Feature 단위 멀티모듈 구조
- Clean Architecture (Domain 중심)
- 상태 / 에러 흐름 일관화

### Module Structure  
android/  
├─ app  
├─ domain  
├─ data  
├─ shared  
└─ feature/  
├─ auth  
├─ scheduler  
├─ mypage  
├─ device  
├─ healthinsight  
├─ map  
├─ news  
└─ chatbot  


자세한 내용은 Android 하위 README 참고.

---

## 🖥 Backend Server

### Responsibilities
- JWT 기반 사용자 인증
- 복약 계획 / 복약 이력 관리
- IoT 센서 데이터 해석
- 복약 상태 판정 (TAKEN / MISSED / WRONG)
- 디바이스 명령 제공
- 비동기 작업 처리 (RAG 등)

### Structure
backend/  
├─ users  
├─ medications  
├─ iot  
├─ health  
├─ rag  
└─ faqs  


---

## 📟 IoT Device (ESP32)

### Firmware Variants
- **PillMyRhythm**
  - LED / Buzzer 기반 알림
- **PillMyRhythm_palette**
  - 슬롯(칸) 기반 복약 안내

### Common Responsibilities
- BLE 기반 초기 등록
- Wi-Fi 연결
- 센서 이벤트 감지
- 서버 ingest 전송
- 서버 command 폴링

### Design Principle
- 디바이스는 이벤트만 발생
- 판단 및 상태 결정은 서버에 위임

---

## 🧠 What This Project Demonstrates
- Android–Server–IoT 통합 시스템 설계
- 명확한 책임 분리
- Clean Architecture의 실전 적용
- 실제 환경을 고려한 연동 구조
- 유지보수와 확장을 고려한 시스템 설계

---

## 📚 Documentation Map

- Android
  - `/android/README.md`
  - `/android/domain/README.md`
  - `/android/feature/*/README.md`

- Backend
  - `/backend/README.md`
  - `/backend/*/README.md`

- IoT
  - `/device/README.md`
  - `/device/PillMyRhythm/README.md`
  - `/device/PillMyRhythm_palette/README.md`

---

## 🚀 Summary

MyRythm은  
단일 앱이 아닌 **역할이 명확히 분리된 통합 시스템**을 목표로 설계되었습니다.

각 구성 요소는 독립적으로 발전할 수 있으며,  
서버를 중심으로 안정적인 연동을 유지합니다.


