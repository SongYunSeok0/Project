#include "SlotLED.h"

int SlotLED::currentSlot = -1;
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
  currentSlot = (currentSlot + 1) % 4;
  slotStartTime = millis();   // ⭐ LED 켜진 시간 기록
  updateSlotLEDs();

  Serial.print("🔔 SLOT LED → ");
  Serial.println(currentSlot);
}

void SlotLED::resetIfTimeout() {
  if (currentSlot < 0) return;

  if (millis() - slotStartTime >= SLOT_ON_DURATION) {
    Serial.println("⏳ Slot LED OFF (10초 만료)");
    currentSlot = -1;
    updateSlotLEDs();   // 모두 OFF
  }
}
