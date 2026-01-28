# MyRythm (Android)

> 복약 스케줄 + 건강(심박/걸음) 모니터링 + 위치/뉴스/챗봇 기능을 한 앱에서 제공하고, 서버/IoT(별도)와 연동되는 멀티모듈 Android 프로젝트입니다.

## 한 줄 요약
- 멀티모듈 + Clean Architecture 기반으로 **Feature 단위 확장/유지보수 가능한 앱 구조**를 설계하고 구현했습니다.

## 뭘 하려고 했는가
- **멀티모듈 분리**: `feature/*` 단위 기능 격리 + 공통 규약은 `shared`로 수렴
- **Clean Architecture 적용**: `domain`(순수 Kotlin) ↔ `data`(API/DB) ↔ `feature`(UI) 경계 유지
- **일관된 결과/에러 모델**: UseCase 중심으로 성공/실패 흐름을 통일(예: ApiResult/DomainError 패턴)
- **외부 SDK 연동**: Naver Map / ML Kit / CameraX / Firebase(FCM) / Kakao & Google Login 등

## Module Map
- `:app`  
  - 앱 엔트리, 테마/스플래시, 전체 네비게이션 조립
- `:domain` *(pure Kotlin)*  
  - 모델/UseCase/Repository 인터페이스, 결과/에러 규약
- `:data`  
  - Retrofit/Moshi, Room, DataStore, FCM 등 실제 구현(Repository Impl)
- `:shared`  
  - 공통 UI 컴포넌트/테마/공통 모델/내비게이션 타입 등
- `:feature:*`  
  - 기능 단위 UI + ViewModel + 화면 흐름  
  - 포함: auth, scheduler, news, map, mypage, healthinsight, chatbot

## App Navigation (개요)
- 메인 진입점에서 각 feature route로 이동하도록 구성
  - ChatBot / Scheduler / Heart(Report) / Map / News / HealthInsight / Profile(Edit) 등

## Tech Stack (Android)
- Kotlin, Jetpack Compose, Navigation
- Hilt (DI)
- Retrofit + Moshi, OkHttp
- Room, DataStore
- Firebase (FCM 등)
- ML Kit (Barcode / Text Recognition), CameraX
- Naver Map SDK, Google/Kakao Login
- (선택) Health Connect, Paging3, Jsoup 등

## Build & Run
### 1) `secret.properties` 생성 (루트에)
`secret.properties`는 Git에 올리지 않습니다.

예시:
NAVER_MAP_CLIENT_ID=...
NAVER_MAP_CLIENT_SECRET=...
NAVER_CLIENT_ID=...
NAVER_CLIENT_SECRET=...
KAKAO_NATIVE_APP_KEY=...
GOOGLE_CLIENT_ID=...


### 2) 서버 Base URL 설정
`data/build.gradle.kts`의 `BACKEND_BASE_URL`을 환경에 맞게 변경합니다.

## Documentation
- 모듈별 상세 문서:
  - [`domain/README.md`](domain/README.md)
  - [`data/README.md`](data/README.md)
  - [`shared/README.md`](shared/README.md)
  - `feature/*/README.md`

## Next (확장 예정)
- 서버/IoT 연동 문서 추가 (API 계약, 디바이스 이벤트 흐름, 시퀀스 다이어그램)

