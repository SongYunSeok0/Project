#include "Sensors.h"
#include "HX711.h"

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
const unsigned long WEIGHT_READ_INTERVAL = 500;   // 최신 버전 반영

bool isOpened = false;
bool openedEvent = false;
unsigned long openedTime = 0;

// ---------------- Time state ----------------
bool isTime = false;
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
// UPDATED CHECKWEIGHT (최신)
// ===================================================
void checkWeight() {
    unsigned long now = millis();

    if (now - lastWeightReadTime < WEIGHT_READ_INTERVAL) return;

    currentWeight = scale.get_units();
    lastWeightReadTime = now;

    float diff = prevWeight - currentWeight;
    prevWeight = currentWeight;

    Serial.printf("Weight: %.2f  Diff: %.2f\n", currentWeight, diff);

    // ---- 약 꺼냄 감지 ----
    if (diff > 100 && !isOpened) {
        isOpened = true;
        openedEvent = true;
        openedTime = now;
        Serial.println("⚠ Weight DROP detected!");

        // ⭐ 정해진 시간에 꺼냈을 때
        if (isTime) {
            isTime = false;

            digitalWrite(19, LOW);  // GREEN OFF
            digitalWrite(18, HIGH); // RED ON

            Serial.println("⏰ Correct time consumption → LED RED!");
        }
        else {
            // 시간 외 오픈 → buzzer
            tone(12, 1000, 800);
        }
    }
}



// ===================================================
// UPDATED RESET LOGIC (최신)
// ===================================================
void handleReset() {
    unsigned long now = millis();

    // ---- GREEN LED 자동 OFF ----
    if (isTime && now - greenStart >= GREEN_DURATION) {
        isTime = false;
        digitalWrite(19, LOW);
        digitalWrite(18, HIGH);
    }

    // ---- opened 상태 자동 해제 ----
    if (isOpened && now - openedTime >= 10000) {
        isOpened = false;
        noTone(12);
    }
}
