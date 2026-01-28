# PillMyRhythm (IoT Firmware)

ESP32 기반 스마트 약통 펌웨어입니다.  
센서 이벤트를 서버로 전송하고, 서버 명령에 따라 사용자에게 복약 안내를 제공합니다.

## Firmware Variants
- `PillMyRhythm/` : 기본형
- `PillMyRhythm_palette/` : 팔레트형

두 버전은 **동일한 통신/판단 구조**를 공유하며,
표시 방식에서만 차이가 있습니다.

## Core Responsibilities
- BLE 기반 초기 등록
- Wi-Fi 연결
- 센서 데이터 수집
- 서버 ingest/command 통신
- 사용자 알림(LED/슬롯)

# Firmware Architecture

## Registration
- BLE를 통해 UUID/TOKEN/Wi-Fi 정보 수신
- 내부 저장 후 재부팅

## Normal Operation
- Wi-Fi 연결
- 센서 업데이트
- 서버 polling
- ingest 전송

## Design Principle
디바이스는 이벤트를 발생시키는 역할만 수행하며,
판단은 서버에 위임합니다.
