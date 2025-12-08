#include "DeviceConfig.h"
#include "BLEConfig.h"
#include "WiFiManager.h"
#include "Sensors.h"
#include "HttpTask.h"
#include "SlotLED.h"

#include <HTTPClient.h>
#include <WiFi.h>
#include <ArduinoJson.h>

QueueHandle_t httpQueue;

// ⭐ 외부로 알려주는 플래그들
bool httpTimeSignal = false;   // loop()에서 "타임 시작"을 한 번만 처리하게 하는 펄스
bool serverTimeFlag = false;   // ⭐ 서버에서 내려준 time:true/false 상태 그대로 저장

const char* postUrl = "http://192.168.0.237:8000/api/iot/ingest/";
const char* getUrl  = "http://192.168.0.237:8000/api/iot/alerts/commands/";

typedef struct {
    bool doPost;
    bool doGet;
    char body[256];
} HttpTaskMessage;

unsigned long lastPostSend = 0;
const unsigned long POST_MIN_INTERVAL = 5000;

unsigned long lastGetSend = 0;
const unsigned long GET_INTERVAL = 10000;

float lastSentBPM = 0;
bool lastSentIsTime = false;


// ===================================================
// HTTP TASK (FreeRTOS)
// ===================================================
void httpTask(void *param) {
    HttpTaskMessage msg;

    for (;;) {
        if (xQueueReceive(httpQueue, &msg, portMAX_DELAY)) {

            // ---------- POST ----------
            if (msg.doPost) {
                HTTPClient http;
                http.begin(postUrl);

                http.addHeader("Content-Type", "application/json; charset=utf-8");
                http.addHeader("X-DEVICE-UUID", DeviceConfig::uuid);
                http.addHeader("X-DEVICE-TOKEN", DeviceConfig::token);

                int code = http.POST((uint8_t*)msg.body, strlen(msg.body));

                Serial.printf("POST code = %d\n", code);
                if (code > 0) {
                    Serial.println("POST response: " + http.getString());
                }
                http.end();
            }

            // ---------- GET ----------
            if (msg.doGet) {
                HTTPClient http;
                http.begin(getUrl);

                http.addHeader("X-DEVICE-UUID", DeviceConfig::uuid);
                http.addHeader("X-DEVICE-TOKEN", DeviceConfig::token);

                int code = http.GET();
                Serial.printf("GET code = %d\n", code);

                if (code == 200) {
                    String res = http.getString();
                    Serial.println("GET response: " + res);
                
                    StaticJsonDocument<128> doc;
                    auto err = deserializeJson(doc, res);
                
                    if (!err) {
                        bool timeFlag = doc["time"] | false;

                        // ⭐ 서버 time 상태를 항상 그대로 저장
                        serverTimeFlag = timeFlag;

                        // ⭐ time:true일 때마다 펄스 한 번 쏴줌
                        //    → loop() 쪽에서 isTime이 이미 true면 무시하니까
                        //      슬롯이 크리스마스 트리처럼 안 바뀜
                        if (timeFlag) {
                            Serial.println("⏰ TIME SIGNAL DETECTED!");
                            httpTimeSignal = true;   // loop()에서 LED, SlotLED 처리
                        }

                    } else {
                        Serial.println("❌ JSON Parse Error");
                    }
                }

                http.end();
            }
        }
    }
} 

// ===================================================
// INIT
// ===================================================
void initHttpTask() {
    httpQueue = xQueueCreate(10, sizeof(HttpTaskMessage));

    xTaskCreatePinnedToCore(
        httpTask,
        "httpTask",
        9000,
        NULL,
        1,
        NULL,
        1
    );

    Serial.println("✔ HTTP Task initialized");
}

// ===================================================
// POST QUEUE
// ===================================================
void queuePost(bool openedEvent, float bpm, bool isTimeParam) {
    if (millis() - lastPostSend < POST_MIN_INTERVAL) return;

    HttpTaskMessage msg;
    msg.doPost = true;
    msg.doGet = false;

    // ⭐ 실제로 서버로 나가는 isTime은 "serverTimeFlag" 기준
    snprintf(
        msg.body,
        sizeof(msg.body),
        "{\"device_uuid\":\"%s\",\"isOpened\":%s,\"isTime\":%s,\"bpm\":%d}",
        DeviceConfig::uuid.c_str(),
        openedEvent ? "true" : "false",
        serverTimeFlag ? "true" : "false",
        (int)bpm
    );

    xQueueSend(httpQueue, &msg, 0);
    lastPostSend = millis();
}

// ===================================================
// GET QUEUE
// ===================================================
void queueGet() {
    HttpTaskMessage msg;
    msg.doPost = false;
    msg.doGet = true;

    xQueueSend(httpQueue, &msg, 0);
}
