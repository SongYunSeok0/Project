# :feature:news

네이버 뉴스 API 기반으로 뉴스 검색/목록/상세(파싱) 등을 제공합니다.

## Scope
- 뉴스 목록/검색 UI
- 이미지 로딩/표시
- Paging3 기반 페이징

## Key Tech
- Retrofit (Gson converter 사용)
- Paging3 (compose)
- Jsoup (상세/본문 파싱 등)

## Config
- `secret.properties`에 `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET` 필요

## 의도
- 페이징/로딩/에러 상태를 UI에서 명확히 다루도록 구성
- 외부 HTML 파싱(Jsoup) 의존을 data/feature에서 적절히 캡슐화
