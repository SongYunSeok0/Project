# rag

약 정보 질의를 처리하는 RAG(Retrieval-Augmented Generation) 모듈입니다.

## Responsibilities
- 자연어 질의 처리
- 벡터 검색(pgvector)
- 비동기 결과 생성

## Async Processing
- 질의 요청은 즉시 task_id를 반환
- 실제 처리는 Celery worker에서 수행
- 결과는 별도 API로 조회

## Why async
응답 시간이 긴 연산을 분리하여
서버 전체 응답 안정성을 유지합니다.
