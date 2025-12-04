#include <Arduino.h>
#include "DeviceConfig.h"
#include "BLEConfig.h"
#include "WiFiManager.h"
#include "Sensors.h"
#include "HttpTask.h"

// --- LED & BUZZER ---
#define RED_LED   18
#define GREEN_LED 19
#define BUZZER    12

void setup() {
    Serial.begin(115200);
    Serial.println("\n=== PillBox v2 (BLE Setup + WiFi + HTTP + Sensors) ===");

    pinMode(RED_LED, OUTPUT);
    pinMode(GREEN_LED, OUTPUT);
    pinMode(BUZZER, OUTPUT);

    digitalWrite(RED_LED, HIGH);
    digitalWrite(GREEN_LED, LOW);
    noTone(BUZZER);

    // ------------------------------------
    // 기존 설정 불러오기
    // ------------------------------------
    DeviceConfig::load();

    // ------------------------------------
    // 등록 여부 판단
    // ------------------------------------
    if (!DeviceConfig::isRegistered()) {
        Serial.println("🔵 등록 필요 → BLE 등록 모드");
        startBLEConfig();           // ⭐ BLE만 켜고, 여기서 끝!
        return;                     // ❗ 절대 아래 실행하면 안 됨
    }

    // -----------------------------
    // 여기까지 왔다면 “이미 등록됨”
    // → WiFi + Sensors + HTTP 시작
    // -----------------------------
    Serial.println("🟢 등록됨 → WiFi 연결 시도");

    connectWiFi();
    initSensors();
    initHttpTask();
}

void loop() {
    // ------------------------------------
    // BLE 등록 모드일 경우
    // ------------------------------------
    if (!DeviceConfig::isRegistered()) {

        // ESP32로부터 JSON 수신 완료됨
        if (bleConfigDone) {
            Serial.println("🟢 BLE 등록 완료!");
            delay(500);

            // 저장된 값으로 재부팅 → 정상 모드 진입
            Serial.println("🔄 재부팅하여 정상 모드로 전환");
            ESP.restart();
        }

        delay(100);
        return;
    }

    // ------------------------------------
    // 정상 운영 모드
    // ------------------------------------
    if (!isWiFiConnected()) {
        delay(200);
        return;
    }

    // 센서 업데이트
    updateBPM();
    checkWeight();
    handleReset();

    // GET 명령 체크
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

    // POST 조건 판단
    static float lastSentBPM = 0;
    static bool lastSentTime = false;

    // POST 조건 판단
bool needPost =
    openedEvent ||
    abs(currentBPM - lastSentBPM) >= 25 ||
    (isTime != lastSentTime);

static bool timeConsumed = false;

if (needPost) {

    // POST 보내기
    queuePost(openedEvent, currentBPM, isTime);

    // ⭐ 정해진 시간에 열린 경우 → POST 후 isTime 끄기
    if (openedEvent && isTime && !timeConsumed) {
        Serial.println("✔ POST sent (isOpened=true, isTime=true) → turn off isTime");

        isTime = false;
        digitalWrite(19, LOW);
        digitalWrite(18, HIGH);
        timeConsumed = true;
    }

    // reset
    openedEvent = false;
    lastSentBPM = currentBPM;
    lastSentTime = isTime;
}


    delay(20);
}
