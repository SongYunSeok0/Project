#pragma once
#include <Arduino.h>

void initHttpTask();
void queuePost(bool openedEvent, float bpm, bool isTime);
void queueGet();

// GET 응답에서 시간 알림이 왔는지 외부에 알려줘야 함
extern bool httpTimeSignal;
