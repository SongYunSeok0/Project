#pragma once
#include <Arduino.h>

// 초기화
void initSensors();

// 주기적 업데이트
void updateBPM();
void checkWeight();
void handleReset();

// 외부에서 참조할 상태값
extern float currentBPM;
extern bool isTime;
extern bool isOpened;
extern bool openedEvent;
