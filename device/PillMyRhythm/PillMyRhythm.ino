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

    // ----------------------------
    // 1) 기존 설정 불러오기
    // ----------------------------
    DeviceConfig::load();

    if (!DeviceConfig::isRegistered()) {
        Serial.println("🔵 등록 필요 → BLE 등록 모드로 진입");
        startBLEConfig();   // BLE 시작 후, 앱이 JSON 보내길 기다림
    } else {
        Serial.println("🟢 등록됨 → WiFi 연결 시도");
        connectWiFi();
    }

    // 센서, HTTP 태스크 준비
    initSensors();
    initHttpTask();
}


void loop() {
    // ----------------------------
    // 2) BLE 등록 완료됐으면 처리
    // ----------------------------
    if (bleConfigDone) {
        bleConfigDone = false;

        Serial.println("🟢 BLE 등록 완료 → WiFi 연결 시작");
        delay(500);

        if (connectWiFi()) {
            Serial.println("✔ PillBox 정상 동작 시작합니다!");
        } else {
            Serial.println("⚠ WiFi 실패 → 재부팅 추천");
        }
    }

    // ----------------------------
    // 3) WiFi 연결 안 됐으면 루프 최소 동작
    // ----------------------------
    if (!isWiFiConnected()) {
        delay(200);
        return;
    }

    // ----------------------------
    // 4) 센서 업데이트
    // ----------------------------
    updateBPM();
    checkWeight();
    handleReset();

    // ----------------------------
    // 5) 서버 → GET 명령 처리
    // ----------------------------
    if (httpTimeSignal) {
        httpTimeSignal = false;

        isTime = true;
        digitalWrite(RED_LED, LOW);
        digitalWrite(GREEN_LED, HIGH);
        Serial.println("💡 TIME SIGNAL: GREEN ON");

        // 타임 상태 유지 시간
        // greenStart 는 Sensors.cpp 안에 있음
        extern unsigned long greenStart;
        greenStart = millis();
    }

    // GET 주기 도달하면 서버 요청
    static unsigned long lastGetSend = 0;
    if (millis() - lastGetSend >= 10000) {
        queueGet();
        lastGetSend = millis();
    }

    // ----------------------------
    // 6) POST 조건 판단 후 전송
    // ----------------------------
    static float lastSentBPM = 0;
    static bool lastSentTime = false;

    bool needPost =
        openedEvent ||
        abs(currentBPM - lastSentBPM) >= 25 ||
        (isTime != lastSentTime);

    if (needPost) {
        queuePost(openedEvent, currentBPM, isTime);
        openedEvent = false;
        lastSentBPM = currentBPM;
        lastSentTime = isTime;
    }

    delay(20);
}
