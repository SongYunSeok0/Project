# medications

복약 계획(Plan)과 복용 이력(RegiHistory)을 관리하는 모듈입니다.

## Responsibilities
- 복약 계획 CRUD
- 복용 이력 기록
- 복약 상태 판단 기준 제공

## Key Concept: Server-side Judgment
복약 여부는 클라이언트나 디바이스가 판단하지 않습니다.

서버는 다음을 기준으로 상태를 결정합니다:
- 복약 예정 시간(taken_at)
- 허용 시간 윈도우
- IoT ingest 이벤트 시점

이를 통해:
- TAKEN
- MISSED
- WRONG
등의 상태를 일관되게 판단합니다.

## Why this matters
복약 판단 로직을 서버에 집중시켜
Android/IoT의 구현 차이로 인한 판단 불일치를 방지합니다.
