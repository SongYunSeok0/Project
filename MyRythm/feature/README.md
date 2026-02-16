# 📦 Feature Modules

`feature` 모듈은 **UI 중심 기능 단위 모듈 집합**입니다.  
각 모듈은 하나의 사용자 기능 흐름을 담당하며,  
Domain 계층의 UseCase만을 의존하도록 설계되었습니다.  

feature  
├── auth  
├── chatbot  
├── healthinsight  
├── map  
├── mypage  
├── news  
└── scheduler  


---

# 🏗 Architecture Principles

## 1. Clean Architecture 기반 설계

- feature → domain 의존
- feature → data 직접 참조 ❌
- Android framework 코드는 feature에만 존재
- 비즈니스 로직은 UseCase를 통해서만 호출


UI → ViewModel → UseCase → Repository(interface)  


---

## 2. Feature 단위 모듈 분리

각 기능은 독립 모듈로 구성되어:

- 확장성 향상
- 기능 단위 테스트 가능
- 의존성 최소화
- 빌드 속도 개선

---

# 📱 Modules Overview

---

## 🔐 auth

로그인 / 회원가입 / 소셜 로그인 기능 담당

### 구성
- LoginViewModel
- SignupViewModel
- PasswordResetViewModel
- SocialLoginViewModel
- AuthNavGraph

### 특징
- UiState + Event 패턴 적용
- UseCase 기반 인증 처리
- 자동 로그인 및 이메일 인증 분리 설계

---

## 🤖 chatbot

AI 챗봇 UI 기능 담당

### 구성
- ChatbotViewModel
- ChatbotScreen
- ChatMessage, ChatEvent

### 특징
- 단방향 데이터 흐름
- 메시지 리스트 상태 관리
- 입력 컴포넌트 분리 설계

---

## 🗺 map

지도 검색 및 위치 기반 기능

### 구성
- MapViewModel
- MapUiState
- PlaceMapper

### 특징
- 검색 헤더 / 리스트 / 바텀시트 분리
- UiState 중심 화면 상태 관리
- Mapper를 통한 Domain 모델 변환

---

## ❤️ healthinsight

사용자 건강 통계 시각화 화면

### 구성
- HealthInsightViewModel
- Chart 컴포넌트 모음
  - HeartRateCard
  - StepsCard
  - MedicationChart
  - HealthBarChart

### 특징
- 통계 데이터 시각화
- 재사용 가능한 Chart 컴포넌트 분리
- Compose recomposition 최소화 설계

---

## 👤 mypage

사용자 관리 및 디바이스 등록 기능

### 구성
- MyPageViewModel
- BLERegisterViewModel
- EditProfileViewModel
- Inquiry / FAQ / Report Screens

### 특징
- QR → BLE → 서버 등록 플로우
- UiState + UiEvent 분리
- BLE 연결은 finally 블록에서 항상 disconnect 처리
- 문의 / 복약 리포트 관리 기능 포함

---

## 📰 news

네이버 뉴스 검색 및 즐겨찾기 기능

### 구성
- NewsViewModel
- Paging 기반 뉴스 리스트
- NewsPagingFactory (feature 내부 인터페이스)
- 즐겨찾기 관리 기능

### 특징
- Paging은 app 계층에서 생성
- feature는 팩토리만 주입
- Clean Architecture 의존성 방향 유지

---

## 📅 scheduler

복약 스케줄 등록 / OCR 카메라 기능

### 구성
- PlanViewModel
- RegiViewModel
- Camera / OCR Screen
- Time Picker Dialog

### 특징
- 카메라 기반 OCR 처리
- 스케줄 등록 로직 분리
- 네비게이션 유틸리티 별도 관리 

---

# 설계 의도

이 프로젝트의 feature 구조는 다음을 목표로 설계되었습니다:

- 기능 단위 모듈화
- Domain 중심 의존성 통제
- UI 상태 패턴 정형화
- 재사용 가능한 Section/Component 분리


