#include <Arduino.h>
#include "SlotLED.h"

void setup() {
  Serial.begin(115200);
  Serial.println("\n=== SlotLED 단독 테스트 모드 ===");

  SlotLED::init();
  Serial.println("🔧 SlotLED 초기화 완료");

  Serial.println("👉 시리얼에 't' 입력 → 다음 슬롯 LED 켜짐");
  Serial.println("👉 10초 지나면 자동 OFF");
}

void loop() {

  // -------------------------
  // 시리얼에서 't' 입력 테스트
  // -------------------------
  if (Serial.available()) {
    char c = Serial.read();
    if (c == 't') {
      Serial.println("📡 입력 감지: 't' → SlotLED::nextSlot()");
      SlotLED::nextSlot();
    }
  }

  // -------------------------
  // 자동 OFF 타이머 (10초)
  // -------------------------
  SlotLED::resetIfTimeout();

  delay(10);
}
