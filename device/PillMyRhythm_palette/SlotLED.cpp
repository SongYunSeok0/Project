#include "SlotLED.h"

int SlotLED::currentSlot = -1;
int SlotLED::lastSlotBeforeOff = -1;   //초기값
unsigned long SlotLED::slotStartTime = 0;

void SlotLED::init() {
  for (int i = 0; i < 4; i++) {
    pinMode(LED_PINS[i], OUTPUT);
    digitalWrite(LED_PINS[i], LOW);
  }
}

void SlotLED::updateSlotLEDs() {
  for (int i = 0; i < 4; i++) {
    digitalWrite(LED_PINS[i], (i == currentSlot) ? HIGH : LOW);
  }
}

void SlotLED::nextSlot() {

  if (currentSlot < 0) {
    // OFF 상태라면 → 마지막 슬롯 이후 번호로 이어가기
    if (lastSlotBeforeOff >= 0)
      currentSlot = (lastSlotBeforeOff + 1) % 4;
    else
      currentSlot = 0;   // 처음 상태일 때만 0에서 시작
  }
  else {
    // 정상적인 순환
    currentSlot = (currentSlot + 1) % 4;
  }

  slotStartTime = millis();
  updateSlotLEDs();

  Serial.print("🔔 SLOT LED → ");
  Serial.println(currentSlot);
}

// ========================================================
//  resetIfTimeout()
// ========================================================
void SlotLED::resetIfTimeout() {

  if (currentSlot < 0) return;

  if (millis() - slotStartTime >= SLOT_ON_DURATION) {

    Serial.println("⏳ Slot LED OFF (10초 만료)");

    // 현재 마지막 슬롯값 저장
    lastSlotBeforeOff = currentSlot;

    // 모두 OFF
    for (int i = 0; i < 4; i++) {
      digitalWrite(LED_PINS[i], LOW);
    }

    // OFF 상태로 전환
    currentSlot = -1;
  }
}

