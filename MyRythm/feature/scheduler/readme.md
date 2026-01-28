# :feature:scheduler

복약 스케줄/기록 입력 흐름을 담당합니다.  
카메라/MLKit(OCR) 기반 입력 기능을 포함합니다.

## Scope
- 스케줄(복약 계획) 입력/조회 UI
- CameraX + MLKit Text Recognition 기반 텍스트 인식(한국어 포함)

## Key Tech
- CameraX
- ML Kit Text Recognition (Korean)
- Room(일부 로컬 저장이 필요한 경우)

## Flow (개요)
Camera/사진 입력
 → 인식 결과 정제
 → 도메인 모델로 변환
 → 저장/동기화(usecase/repo)

## 의도
- 디바이스 기능(카메라/OCR)을 feature 내부로 한정
- UI/도메인 로직 분리를 유지하면서 입력 파이프라인 구성
