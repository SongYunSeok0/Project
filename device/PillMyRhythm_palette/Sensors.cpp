#include "Sensors.h"
#include "HX711.h"
#include "HttpTask.h"   // ⭐ serverTimeFlag 사용

// ---------------- HX711 ----------------
#define DOUT  4
#define CLK   5
#define CALIBRATION_FACTOR -380.0
HX711 scale(DOUT, CLK);

// ---------------- Heartbeat ----------------
const int SENSOR_PIN = 35;
const unsigned long BPM_INTERVAL = 10000;
const unsigned long MIN_GAP = 300;

unsigned long lastBeat = 0;
unsigned long bpmStartTime = 0;
int beats = 0;
int baseline = 0;
bool isAbove = false;

float currentBPM = 0;
bool fingerPresent = false;


// ---------------- Weight ---------------------
float currentWeight = 0;
float prevWeight = 0;
unsigned long lastWeightReadTime = 0;
const unsigned long WEIGHT_READ_INTERVAL = 5000;   // 최신 버전 반영

bool isOpened = false;       // 최근 10초 안에 열렸는지
bool openedEvent = false;    // 이번 루프에서 서버로 보낼 열림 이벤트 플래그
unsigned long openedTime = 0;

// ---------------- Time state (LED용) ----------------
bool isTime = false;         // ⭐ LED / SlotLED용 로컬 타임 상태
unsigned long greenStart = 0;
const unsigned long GREEN_DURATION = 10000;


// ---------------- Utils ---------------------
int readSmooth(int pin, int samples = 15) {
    long sum = 0;
    for (int i = 0; i < samples; i++) {
        sum += analogRead(pin);
        delay(2);
    }
    return sum / samples;
}



// ===================================================
// INIT
// ===================================================
void initSensors() {
    analogReadResolution(12);
    analogSetPinAttenuation(SENSOR_PIN, ADC_11db);

    scale.set_scale(CALIBRATION_FACTOR);
    scale.tare();
    prevWeight = scale.get_units();

    bpmStartTime = millis();

    Serial.println("✔ Sensors initialized");
}



// ===================================================
// BPM UPDATE
// ===================================================
void updateBPM() {
    unsigned long now = millis();
    int val = readSmooth(SENSOR_PIN);

    fingerPresent = (val > 500);
    if (!fingerPresent) {
        baseline = 0;
        beats = 0;
        currentBPM = 0;
        bpmStartTime = now;
        isAbove = false;
        return;
    }

    if (baseline == 0) baseline = val;
    else baseline = (baseline * 19 + val) / 20;

    int threshold = baseline + 10;
    const unsigned long LOCAL_MIN_GAP = 550;

    if (!isAbove && val > threshold && (now - lastBeat) > LOCAL_MIN_GAP) {
        isAbove = true;
        lastBeat = now;
        beats++;
    } 
    else if (isAbove && val < baseline) {
        isAbove = false;
    }

    if (now - bpmStartTime >= BPM_INTERVAL) {
        unsigned long window = now - bpmStartTime;
        float instantBPM = (window > 0) ? beats * (60000.0 / window) : 0;

        bpmStartTime = now;
        beats = 0;

        if (currentBPM == 0) currentBPM = instantBPM;
        else currentBPM = currentBPM * 0.6f + instantBPM * 0.4f;
    }
}



// ===================================================
// UPDATED CHECKWEIGHT
// ===================================================
void checkWeight() {
    unsigned long now = millis();

    if (now - lastWeightReadTime < WEIGHT_READ_INTERVAL) return;

    currentWeight = scale.get_units();
    lastWeightReadTime = now;

    float diff = prevWeight - currentWeight;
    prevWeight = currentWeight;

    float adiff = fabs(diff);
    Serial.printf("Weight: %.2f  Diff: %.2f\n", currentWeight, adiff);

    // -----------------------------
    // 약 꺼냄 감지 (이 순간만 openedEvent = true)
    // -----------------------------
    if (adiff > 100) {
        openedEvent = true;      // 서버에 보낼 이벤트
        openedTime = now;

        Serial.println("⚠ Weight DROP detected!");

        // 최근 10초 동안 열림 상태 유지
        isOpened = true;

        // ⭐ 여기서 "정해진 시간" 판단은
        //    서버에서 내려준 serverTimeFlag 기준
        if (serverTimeFlag) {
            Serial.println("⏰ Correct time consumption (serverTimeFlag=true) → LED RED");
            digitalWrite(19, LOW);   // GREEN OFF
            digitalWrite(18, HIGH);  // RED ON
        } else {
            Serial.println("🚨 Wrong time → buzzer");
            tone(12, 1000, 800);
        }
    }
}



// ===================================================
// UPDATED RESET LOGIC
// ===================================================
void handleReset() {
    unsigned long now = millis();

    // GREEN LED 자동 OFF (LED용 타임 끝)
    if (isTime && now - greenStart >= GREEN_DURATION) {
        isTime = false;          // ⭐ LED용 상태 종료 (다음 time:true 받을 준비)
        digitalWrite(19, LOW);
        digitalWrite(18, HIGH);
    }

    // 열림 상태 자동 해제 (10초 뒤)
    if (isOpened && now - openedTime >= 10000) {
        isOpened = false;
        noTone(12);
    }
}
