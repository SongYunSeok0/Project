#pragma once
#include <Arduino.h>

namespace SlotLED {

static const int LED_PINS[4] = {25, 26, 27, 33};
extern int currentSlot;

// LED 유지 시간
extern unsigned long slotStartTime;
const unsigned long SLOT_ON_DURATION = 10000;  // 10초

void init();
void updateSlotLEDs();
void nextSlot();   // time:true 들어오면 실행
void resetIfTimeout();  // 10초 지나면 OFF
}
