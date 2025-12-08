#pragma once
#include <Arduino.h>

namespace SlotLED {

static const int LED_PINS[4] = {25, 26, 27, 33};

extern int currentSlot;
extern int lastSlotBeforeOff;   // ⭐ 추가: OFF되기 전에 마지막 슬롯 번호 저장
extern unsigned long slotStartTime;

const unsigned long SLOT_ON_DURATION = 10000;

void init();
void updateSlotLEDs();
void nextSlot();
void resetIfTimeout();
}

