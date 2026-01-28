# users

사용자 인증 및 계정 관리를 담당하는 모듈입니다.

## Responsibilities
- 회원가입 / 로그인
- JWT 발급 및 갱신
- 사용자 정보 조회/수정
- FCM 토큰 등록
- 이메일 인증 코드 발송/검증

## Why this module exists
인증과 권한 관리는 모든 기능의 기반이 되므로,
다른 도메인(복약, IoT 등)과 분리된 단일 책임 모듈로 관리됩니다.

## Key Concepts
- 인증은 JWT 기반으로 처리됩니다.
- 인증 여부는 API 접근 권한의 기준이 됩니다.
