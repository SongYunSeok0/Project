# PillMyRhythm (Basic)

기본형 스마트 약통 펌웨어입니다.

## Responsibilities
- BLE 등록 모드 제공
- Wi-Fi 연결 및 상태 유지
- 센서 이벤트 감지
- LED / Buzzer 알림

## Runtime Flow
1. 등록 여부 확인
2. 미등록 시 BLE 등록 모드
3. 등록 완료 후 정상 모드
4. 서버 명령에 따라 LED 알림
