#include <Arduino.h>
#include "DeviceConfig.h"
#include "BLEConfig.h"
#include "WiFiManager.h"
#include "Sensors.h"
#include "HttpTask.h"
#include "SlotLED.h"

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

    // 센서 업데이트
    updateBPM();
    checkWeight();
    handleReset();

    // 10초 후 슬롯 LED 자동 OFF
    SlotLED::resetIfTimeout();

    // -------------------------------------------------
    // GET 명령 처리 (time:true)
    // -------------------------------------------------
    if (httpTimeSignal) {
        httpTimeSignal = false;

        isTime = true;

        digitalWrite(RED_LED, LOW);
        digitalWrite(GREEN_LED, HIGH);

        Serial.println("💡 TIME SIGNAL: GREEN ON");

        extern unsigned long greenStart;
        greenStart = millis();
    }

    // GET 요청 주기
    static unsigned long lastGetSend = 0;
    if (millis() - lastGetSend >= 10000) {
        queueGet();
        lastGetSend = millis();
    }

    // -------------------------------------------------
    // POST 조건 판단
    // -------------------------------------------------
    static float lastSentBPM = 0;
    static bool lastSentTime = false;
    static bool timeConsumed = false;

    bool needPost =
        openedEvent ||
        abs(currentBPM - lastSentBPM) >= 25 ||
        (isTime != lastSentTime);

    if (needPost) {

        // POST 보내기
        queuePost(openedEvent, currentBPM, isTime);

        // 정해진 시간에 열렸으면 isTime OFF
        if (openedEvent && isTime && !timeConsumed) {
            Serial.println("✔ POST sent (isOpened=true, isTime=true) → turn off isTime");

            isTime = false;
            digitalWrite(GREEN_LED, LOW);
            digitalWrite(RED_LED, HIGH);
            timeConsumed = true;
        }

        openedEvent = false;
        lastSentBPM = currentBPM;
        lastSentTime = isTime;
    }

    delay(20);
}
