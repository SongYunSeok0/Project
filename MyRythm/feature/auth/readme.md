# :feature:auth

로그인/회원가입 및 소셜 로그인(Kakao/Google)을 담당합니다.

## Scope
- 로그인/회원가입 UI
- 소셜 로그인 연동
- 인증 상태 기반 내비게이션 분기

## Flow (개요)
UI(Compose)
 → ViewModel
 → UseCase(domain)
 → Repository(domain interface)
 → data(repository impl / api / local)

## Config
- `secret.properties`에 `GOOGLE_CLIENT_ID`, `KAKAO_NATIVE_APP_KEY` 필요

## 의도
- 소셜 로그인 SDK를 UI에 직접 섞지 않고, ViewModel/UseCase 중심 흐름으로 정리
- 인증 상태에 따라 앱 진입 동선을 안정적으로 유지
