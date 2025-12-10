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

const int THRESHOLD_OFFSET = 5;

// -------------- Weight ---------------------
float currentWeight = 0;
float prevWeight = 0;
unsigned long lastWeightReadTime = 0;
const unsigned long WEIGHT_READ_INTERVAL = 5000;

const unsigned long OPEN_IGNORE_DURATION = 300;

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

    // baseline 계산 (미세하게 둔감하게)
    if (baseline == 0) baseline = val;
    else baseline = (baseline * 14 + val) / 15;  // ← 9 → 14로 변경

    int threshold = baseline + 8;                // ← +6 → +8 조정
    const unsigned long LOCAL_MIN_GAP = 450;     // ← 380 → 450ms

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
// WEIGHT CHECK
// ===================================================
void checkWeight() {
    unsigned long now = millis();

    // 최근 opened 이후 3초간 무게 변화 무시
    if (isOpened && (now - openedTime < OPEN_IGNORE_DURATION)) {
        return;
    }

    // 무게 읽기 주기 (2초)
    if (now - lastWeightReadTime < WEIGHT_READ_INTERVAL) return;

    currentWeight = scale.get_units();
    lastWeightReadTime = now;

    float diff = currentWeight - prevWeight;
    
    // 디버깅 출력
    Serial.print("[WEIGHT] current=");
    Serial.print(currentWeight);
    Serial.print(" diff=");
    Serial.println(diff);

    prevWeight = currentWeight;

    // 무게 증가 -> 열림 이벤트 발생
    if (diff > 100 && !isOpened) {
        isOpened = true;
        openedEvent = true;
        openedTime = now;

        Serial.println("📦 OPEN DETECTED (diff > 100)!");

        if (isTime) {
            Serial.println("⏰ Scheduled opening at the correct time!");
        } else {
            tone(12, 1000, 800);
        }
    }
}







// ===================================================
// RESET LOGIC
// ===================================================
void handleReset() {
    unsigned long now = millis();

    if (isTime && now - greenStart >= GREEN_DURATION) {
        isTime = false;
        digitalWrite(19, LOW);
        digitalWrite(18, HIGH);
    }

    if (isOpened && now - openedTime >= 10000) {
        isOpened = false;
        noTone(12);
    }
}
