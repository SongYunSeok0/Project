# :data

네트워크/DB/로컬저장소 등 “구현”을 담당하는 계층입니다.  
domain의 Repository 인터페이스를 구현하고, 외부 시스템과의 연결을 캡슐화합니다.

## Responsibility
- Retrofit/Moshi 기반 API 통신
- Room 기반 로컬 DB
- DataStore(Preferences) 기반 저장(예: 자동로그인/토큰 등)
- Firebase Messaging(FCM) 연동
- DTO ↔ Domain Mapper

## Key Dependencies
- Retrofit + Moshi, OkHttp Logging Interceptor
- Room
- DataStore Preferences
- Firebase (Messaging)

## Configuration
### Backend Base URL
`data/build.gradle.kts`:
- `BACKEND_BASE_URL`을 로컬/서버 환경에 맞게 교체

### External API Keys
- Naver News / Naver Map 키는 `secret.properties`에서 주입

## Recommended Structure
- `network/api` : API interfaces
- `network/dto` : DTO
- `db/dao`, `db/entity` : Room
- `repository/*Impl` : Repository 구현체
- `mapper/*` : DTO/Entity ↔ Domain 변환
- `core/*` : 공통 네트워크/인증 인터셉터 등

## Error Handling (권장 규약)
- data는 Throwable/HTTP 에러를 domain 규약(예: DomainError)로 변환
- feature는 구현 세부사항을 몰라도 “의미 있는 실패”로 처리 가능해야 함
