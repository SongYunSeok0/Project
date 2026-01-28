# :feature:map

Naver Map SDK 기반 지도 화면 및 위치 권한/현재 위치 기능을 제공합니다.

## Scope
- 지도 UI(Compose 연동)
- 위치 권한 처리
- 현재 위치 기반 UX

## Key Tech
- Naver Map SDK
- Play Services Location
- Accompanist Permissions

## Config
- `secret.properties`에 `NAVER_MAP_CLIENT_ID` 필요 (manifest placeholder)

## 의도
- 권한/위치/지도 렌더링을 feature에 격리해 유지보수성 확보
- Map SDK 의존이 다른 feature로 전파되지 않도록 모듈 경계 유지
