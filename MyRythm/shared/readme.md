# :shared

여러 feature가 공통으로 사용하는 **UI/모델/규약**을 제공합니다.

## Responsibility
- Compose Theme / 공통 컴포넌트
- 공통 UI 모델(UiError 등)
- 공통 Navigation 타입(예: MainRoute 등)
- 리소스(shared 모듈 리소스) 및 공통 유틸

## Why shared?
- feature 간 직접 참조를 줄이고, 공통 규약을 한 곳에 모아
  의존성 역전을 방지하기 위함

## Usage
- feature 모듈은 필요한 공통 UI 요소만 shared에 의존
- domain에 없는 “표현 계층 전용” 모델은 shared에 배치
