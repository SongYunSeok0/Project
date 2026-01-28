# smart_med (Backend)

MyRythm 시스템의 백엔드 서버입니다.  
Android 앱과 IoT 디바이스 사이에서 **중앙 제어자(Control Plane)** 역할을 수행합니다.

## Role in System
- Android: 사용자 인터페이스 및 사용자 트리거
- IoT Device: 센서 이벤트 생성
- **Server**:
  - 데이터 영속화
  - 복약 상태 판단
  - 디바이스 인증
  - 명령 분배
  - 비동기 작업 처리

서버는 단순 저장소가 아니라,
**시스템의 상태를 판단하고 일관성을 유지하는 주체**입니다.

---

## Core Responsibilities
- 사용자 인증 및 권한 관리 (JWT)
- 복약 계획/이력 관리
- IoT 디바이스 연동 및 센서 데이터 해석
- 건강 데이터 관리 (심박, 걸음)
- 비동기 작업 처리 (RAG, 알림 등)

---

## Architecture Overview
- Framework: Django + Django REST Framework
- Auth: JWT (SimpleJWT)
- DB: PostgreSQL (+ pgvector)
- Async: Celery + Redis
- Notification: Firebase Cloud Messaging (FCM)
- API Docs: drf-spectacular (Swagger/Redoc)

---

## App Structure  
smart_med/
├─ users/ # 인증 및 사용자
├─ medications/ # 복약 계획/이력
├─ iot/ # 디바이스 연동
├─ health/ # 심박/걸음 데이터
├─ rag/ # 약 정보 질의 (RAG)
├─ faqs/ # FAQ  


각 앱은 **하나의 도메인 책임만** 갖도록 분리되어 있으며,
서로의 내부 구현에 직접 의존하지 않습니다.

