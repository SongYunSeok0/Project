#include <Arduino.h>
#include "DeviceConfig.h"
#include "BLEConfig.h"
#include "WiFiManager.h"
#include "Sensors.h"
#include "HttpTask.h"
#include "SlotLED.h"

extern unsigned long greenStart;

// --- LED & BUZZER ---
#define RED_LED   18
#define GREEN_LED 19
#define BUZZER    12

void setup() {
    Serial.begin(115200);
    Serial.println("\n=== PillBox v2 (BLE Setup + WiFi + HTTP + Sensors + SlotLED) ===");

    pinMode(RED_LED, OUTPUT);
    pinMode(GREEN_LED, OUTPUT);
    pinMode(BUZZER, OUTPUT);

    digitalWrite(RED_LED, HIGH);
    digitalWrite(GREEN_LED, LOW);
    noTone(BUZZER);

    // ---------------------------------
    // 🔧 저장된 설정 로드
    // ---------------------------------
    DeviceConfig::load();

    Serial.println("===== STORED DEVICE CONFIG =====");

    Serial.print("UUID: ");
    Serial.println(DeviceConfig::uuid);
    
    Serial.print("TOKEN: ");
    Serial.println(DeviceConfig::token);
    
    Serial.print("SSID: ");
    Serial.println(DeviceConfig::ssid);
    
    Serial.print("PW: ");
    Serial.println(DeviceConfig::pw);
    
    Serial.println("================================");
    

    // ---------------------------------
    // 등록 여부 확인
    // ---------------------------------
    if (!DeviceConfig::isRegistered()) {
        Serial.println("🔵 등록 필요 → BLE 등록 모드");
        startBLEConfig();
        return;
    }

    // ---------------------------------
    // WiFi 정보 확인
    // ---------------------------------
    if (!DeviceConfig::hasWiFiInfo()) {
        Serial.println("⚠ WiFi 정보 없음 → BLE 등록 필요");
        startBLEConfig();
        return;
    }

    Serial.println("🟢 등록됨 → WiFi 연결 시도");
    connectWiFi();
    initSensors();
    initHttpTask();
    SlotLED::init();
}

void loop() {

    // -------------------------------------------------
    // 등록되지 않은 경우 → BLE 설정 대기
    // -------------------------------------------------
    if (!DeviceConfig::isRegistered()) {

        if (bleConfigDone) {
            Serial.println("🟢 BLE 등록 완료!");
            delay(500);
            Serial.println("🔄 재부팅하여 정상 모드로 전환");
            ESP.restart();
        }

        delay(100);
        return;
    }

    // -------------------------------------------------
    // 정상 운영
    // -------------------------------------------------
    if (!isWiFiConnected()) {
        delay(200);
        return;
    }

    // -------------------------------------------------
    // 🔵 (0) 서버 time:true 펄스 먼저 처리
    // -------------------------------------------------
    if (httpTimeSignal) {
        httpTimeSignal = false;

    // isTime 여부 상관없이 슬롯 이동
        SlotLED::nextSlot();

    // GREEN LED 처리
        if (!isTime) {
            isTime = true;
            digitalWrite(RED_LED, LOW);
            digitalWrite(GREEN_LED, HIGH);
            greenStart = millis();
            Serial.println("💡 TIME SIGNAL: GREEN ON");
        }
    }


    // 센서 업데이트
    updateBPM();
    checkWeight();
    handleReset();

    // 10초 후 슬롯 LED 자동 OFF
    SlotLED::resetIfTimeout();

    // -------------------------------------------------
    // 🔵 (1) 시리얼 입력으로 time:true 테스트
    // -------------------------------------------------
    if (Serial.available()) {
        char c = Serial.read();
        if (c == 't') {
            Serial.println("📡 SERIAL: time:true RECEIVED → SlotLED::nextSlot()");
            SlotLED::nextSlot();
        }
    }

    // -------------------------------------------------
    // GET 요청 주기 (10초)
    // -------------------------------------------------
    static unsigned long lastGetSend = 0;
    if (millis() - lastGetSend >= 18000) {
        queueGet();
        lastGetSend = millis();
    }

    // -------------------------------------------------
    // POST 조건 판단
    // -------------------------------------------------
    static float lastSentBPM = 0;

    bool needPost =
        openedEvent ||
        abs(currentBPM - lastSentBPM) >= 25;

    if (needPost) {

        // ⭐ POST에 들어가는 isTime은 "serverTimeFlag" 기준
        queuePost(openedEvent, currentBPM, serverTimeFlag);

        if (openedEvent) {
            openedEvent = false;
        }

        lastSentBPM = currentBPM;
    }

    delay(20);
}
