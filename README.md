# 💊 스마트 복약 관리 시스템  
무게 기반 복용 감지 + 의약품 LLM 질의응답 + 처방전 OCR 스케줄러

**“무게 변화 + 의약품 지식 + 처방전 OCR로 복약 관리 전 과정을 자동화하는 All-in-One 시스템”**

- HX711 로드셀 기반 **복용 여부 자동 감지**  
- **OTC 의약품 지식 LLM + RAG 기반 Q&A**  
- **처방전 촬영 → OCR → 자동 스케줄 생성**  
- FCM 기반 **정확한 복약 알림**  
- IoT(Arduino/LoRa) → Django → Android 앱까지 **엔드투엔드 통합**

---

<h2>🏗️ 전체 시스템 아키텍처</h2>

<pre>
[스마트 약통 (Arduino/ESP32 + HX711)]
 └─ Wi-Fi
 ├─ 무게 수집
 ├─ EWMA 필터링
 ├─ 무게 기반 복용 판별
 └─ 서버 전송 (HTTP)
      ↓
[Django Backend + PostgreSQL]
 
 ├─ OTC-QA (RAG 임베딩 검색)
 ├─ OCR 처방 파싱 → 스케줄 생성
 └─ FCM 알림 트리거
      ↓
[Android 앱 (Kotlin + Compose)]
 ├─ 복약 현황 / 스케줄 UI
 ├─ OTC-QA 챗봇
 ├─ 프로필 / FCM / 설정
 └─ 클라우드 동기화
</pre>


<h2>🧩 Android 멀티모듈 구조</h2>

<pre>
app/
 ├─ presentation (NavHost)
 ├─ di
 └─ remote (api)

feature/
 ├─ main
 ├─ map
 ├─ news
 ├─ scheduler (OCR 플로우)
 ├─ camera
 └─ mypage (프로필 / FAQ / 문의)

domain/
 ├─ model
 ├─ repository
 └─ usecase

data/
 ├─ repository
 ├─ retrofit dto
 ├─ mapper
 └─ room (Inquiry 등)

core/
 ├─ auth (TokenStore)
 ├─ push (FCM)
 └─ util

common/design/
 └─ UI 컴포넌트 (TopBar / BottomBar)
</pre>


---

## ⚙️ Backend 구성

### ✔ Django + DRF
- JWT 인증  
- IoT 무게 데이터 수신  
- 복약 기록 저장  
- FCM 토큰 관리  
- OCR 파싱 → 스케줄 생성  
- OTC-QA 엔드포인트  

### ✔ AI 모듈
- RAG 기반 OTC 의약품 질의응답  
- OCR 처방전 텍스트 파싱  
- 복용 여부 AI 모델  

### ✔ Infra
- PostgreSQL  
- Redis + Celery  
- pgvector (임베딩 검색)

---

## 🤖 핵심 AI 기능

### 1) 무게 기반 복용 여부 판별
- EWMA + 중앙값 필터  
- “뚜껑 열림/진동” vs “실제 복용” 패턴 분리  
- 로우데이터 노이즈 제거

### 2) OTC-QA (의약품 질의응답)
- 식약처 / 약학정보원 데이터 기반  
- LLM + RAG 구성  
- 임산부/소아주의·중복성분 자동 경고

### 3) 처방전 OCR & 스케줄 자동 생성
- OCR → 용법 파싱  
- “1일 3회 5일간 → RRULE 일정 생성”  
- 모호한 처방은 사용자 확인 후 생성

### 4) 개인화 복약 알림 (FCM)
- Django → Firebase FCM  
- 스케줄 기반 알람  
- 복용 패턴 기반 개인화(확장 가능)

---

## 🔔 전체 동작 플로우
HX711 무게 변화 감지

IoT Gateway(EWMA 필터) → 서버 전송

Django AI: 복용 여부 판별

기록 저장 → FCM 알림

앱에서 복약 현황/스케줄 표시

처방전 촬영 → OCR → 스케줄 생성

OTC-QA 챗봇 질의응답


---

## 🛡️ 예외 처리 전략

### IoT/센서
- 스파이크 제거  
- 저전력 환경 대비 로컬 큐 저장 후 재전송  
- 자동 영점 재보정

### 네트워크
- HTTP 실패 → 지수 백오프  
- MQTT 폴백  
- event_id(UUID) 기반 중복 업로드 방지

### 서버
- Celery 지수 재시도  
- 스케줄 생성 시 트랜잭션 보장  
- 단위/타임존 검증

### 앱
- Retrofit 에러 핸들링  
- JWT 자동 Refresh  
- 무효 FCM 즉시 제거  
- OCR 실패 시 수동 입력 fallback

---

## 🧠 추가 기능 — 객체 인식 기반 사용주기 트래커

APP 내 별도 AI 기능

- YOLO/SSD → 객체 탐지  
- CLIP/MobileNet → 임베딩  
- FAISS → 동일 물건 매칭  
- 세션 간격 → 사용주기 계산  
- 30·60·90일 미사용 알림 제공  

[카메라] → [탐지] → [임베딩/FAISS] → [세션 기록] → [주기 계산]


---

## 📦 기술 스택

### Android
- Kotlin, Jetpack Compose  
- Hilt, Retrofit, Room  
- Firebase FCM  
- CameraX, ML Kit OCR  

### Backend
- Django, DRF  
- PostgreSQL  
- Redis, Celery  
- pgvector  
- LangChain (RAG)  
- Tesseract/Google OCR  

### IoT
- Arduino/ESP32 + HX711  
- Python  
- HTTP

---

## 📊 MVP 난이도

| 기능 | 난이도 | 비고 |
|------|--------|------|
| 무게 기반 복약 감지 | 3.0 | 필터링/센서 안정화 |
| OTC-QA | 3.0 | 데이터 구축 + RAG |
| 처방전 OCR | 3.5 | 용법 파싱 난이도 |
| 사용주기 트래커 | 3.5 | ReID + 통계 |

---

