#pragma once
#include <Arduino.h>

void initHttpTask();
void queuePost(bool openedEvent, float bpm, bool isTimeParam);
void queueGet();

// GET에서 내려온 time:true 펄스 → loop()에서 처리
extern bool httpTimeSignal;

// 서버가 내려주는 time 상태 (그대로 서버에 되돌려 보내는 용도)
extern bool serverTimeFlag;
