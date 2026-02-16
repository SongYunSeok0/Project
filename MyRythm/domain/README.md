# :domain

UI/Android 의존성이 없는 **순수 Kotlin 모듈**입니다.  
앱의 “규칙(정책)”을 정의하고, 다른 계층이 이를 따르게 합니다.

## Responsibility
- 도메인 모델(Entity/Value)
- UseCase (앱 유스케이스)
- Repository 인터페이스
- 결과/에러 규약 (예: ApiResult/DomainError 패턴)

## Design Principles
- **순수성**: Android framework / Retrofit / Room 등 구현 디테일에 의존하지 않음
- **일관된 흐름**: `UI(ViewModel) → UseCase → Repository(Interface)`로 고정
- **테스트 용이성**: Repository를 Fake로 대체해 UseCase 단위 테스트가 쉬움

## Package Guide
- `model/` : 도메인 모델
- `repository/` : Repository interfaces
- `usecase/**` : 유스케이스 (기능별 폴더)
- `result/` or `error/` : 결과/에러 규약

## How to use
- feature(ViewModel)에서는 구현체가 아닌 UseCase만 의존
- data 모듈은 domain의 Repository 인터페이스를 구현
