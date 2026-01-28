# iot

IoT 디바이스와의 통신 및 디바이스 상태 관리를 담당하는 모듈입니다.

## Responsibilities
- 디바이스 인증 (UUID / TOKEN)
- 센서 데이터 수신 (ingest)
- 디바이스 명령 제공 (command polling)
- 디바이스–사용자 연결 관리

## Device Authentication
IoT 디바이스는 다음 헤더로 인증합니다:
- X-DEVICE-UUID
- X-DEVICE-TOKEN

이를 통해 사용자 인증(JWT)과 분리된
**디바이스 전용 인증 체계**를 유지합니다.

## Ingest Flow (Device → Server)
1. 디바이스가 센서 이벤트를 POST
2. 서버는 이벤트를 해석
3. 복약 계획과 비교하여 상태 판정
4. 결과를 복약 이력 및 건강 데이터로 저장

## Command Flow (Server → Device)
- 디바이스는 주기적으로 서버에 명령을 폴링
- 서버는 복약 시간 도래 여부를 전달
- 디바이스는 LED/슬롯 표시로 사용자에게 안내

## Design Principle
IoT 디바이스는 판단하지 않습니다.
**모든 판단은 서버에서 수행됩니다.**
