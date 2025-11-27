#include <WiFi.h>
#include <HTTPClient.h>
#include "HX711.h"

// ------------------ HX711 설정 ------------------
#define DOUT  4
#define CLK   5
#define CALIBRATION_FACTOR -380.0
HX711 scale(DOUT, CLK);

// ------------------ 맥박 센서 ------------------
const int SENSOR_PIN = 35;
const unsigned long BPM_INTERVAL = 10000;
const unsigned long MIN_GAP = 300;
const int THRESHOLD_OFFSET = 5;

unsigned long lastBeat = 0;
unsigned long bpmStartTime = 0;
int beats = 0;
int baseline = 0;
bool isAbove = false;
float currentBPM = 0;

// ------------------ WiFi & 서버 ------------------
const char* ssid = "sesac";
const char* password = "12345678";

const char* postUrl = "http://192.168.0.154:8000/api/iot/ingest/";
const char* getUrl  = "http://192.168.0.154:8000/api/iot/alerts/commands/";

const char* DEVICE_UUID  = "2cac933d85a51608";
const char* DEVICE_TOKEN = "97211228f1fa705b3b3750f3c7693f3de4086e0b9050aa8df3c1e459e4f1f133";  // 실제 토큰 넣기

// ------------------ LED, 부저 ------------------
#define RED_LED 18
#define GREEN_LED 19
#define BUZZER 12

// ------------------ Weight ------------------
float currentWeight = 0;
float prevWeight = 0;
unsigned long lastWeightReadTime = 0;
const unsigned long WEIGHT_READ_INTERVAL = 500;
const unsigned long HX711_BLOCK_MS = 80;

bool isOpened = false;
unsigned long openedTime = 0;

// ------------------ Time 상태 ------------------
bool isTime = false;
unsigned long greenStart = 0;
const unsigned long GREEN_DURATION = 10000;

// POST 상태 체크
bool lastIsOpened = false;
bool lastIsTime = false;
float lastBPM = 0;

// GET 주기
unsigned long lastGetTime = 0;
const unsigned long GET_INTERVAL = 10000;

// ===================================================
// Setup
// ===================================================
void setup() {
  Serial.begin(115200);
  Serial.println("\n=== PillBox + Heartbeat ESP32 ===");

  pinMode(RED_LED, OUTPUT);
  pinMode(GREEN_LED, OUTPUT);
  pinMode(BUZZER, OUTPUT);

  digitalWrite(RED_LED, HIGH);
  digitalWrite(GREEN_LED, LOW);
  digitalWrite(BUZZER, LOW);

  analogReadResolution(12);
  analogSetPinAttenuation(SENSOR_PIN, ADC_11db);

  // WiFi 연결
  WiFi.begin(ssid, password);
  Serial.print("WiFi connecting...");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("\nWiFi Connected!");
  Serial.println(WiFi.localIP());

  // HX711 초기화
  scale.set_scale(CALIBRATION_FACTOR);
  scale.tare();
  prevWeight = scale.get_units();

  bpmStartTime = millis();
}

// ===================================================
// 심박 측정
// ===================================================
int readSmooth(int pin, int samples = 15) {
  long sum = 0;
  for (int i = 0; i < samples; i++) {
    sum += analogRead(pin);
    delay(2);
  }
  return sum / samples;
}

void updateBPM() {
  unsigned long now = millis();

  if (now - lastWeightReadTime < HX711_BLOCK_MS) return;

  int val = readSmooth(SENSOR_PIN);

  if (baseline == 0) baseline = val;
  baseline = (baseline * 19 + val) / 20;

  int threshold = baseline + THRESHOLD_OFFSET;

  if (val > threshold && (now - lastBeat) > MIN_GAP && !isAbove) {
    beats++;
    lastBeat = now;
    isAbove = true;
  } else if (val < baseline) {
    isAbove = false;
  }

  if (now - bpmStartTime >= BPM_INTERVAL) {
    currentBPM = beats * (60000.0 / BPM_INTERVAL);
    Serial.printf("BPM: %.1f\n", currentBPM);
    beats = 0;
    bpmStartTime = now;
  }
}

// ===================================================
// 서버로 데이터 전송 (HEADERS 인증 방식!!)
// ===================================================
void sendDataToServer() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  http.begin(postUrl);

  // ⭐ 인증 헤더 추가
  http.addHeader("X-DEVICE-UUID",  DEVICE_UUID);
  http.addHeader("X-DEVICE-TOKEN", DEVICE_TOKEN);
  http.addHeader("Content-Type", "application/json");

  // JSON body
  String json = "{";
json += "\"device_uuid\":\"2cac933d85a51608\",";  // 여기에 실제 uuid
json += "\"isOpened\":" + String(isOpened ? "true" : "false") + ",";
json += "\"isTime\":" + String(isTime ? "true" : "false") + ",";
json += "\"bpm\":" + String((int)currentBPM);
json += "}";

  Serial.println("POST JSON = " + json);

  int code = http.POST(json);

  Serial.print("POST response: ");
  Serial.println(code);

  if (code > 0) {
    Serial.println("Server: " + http.getString());
  }

  http.end();
}

// ===================================================
// 서버에서 명령 받기 (헤더 인증 포함!)
// ===================================================
void getCommandFromServer() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  http.begin(getUrl);

  http.addHeader("X-DEVICE-UUID",  DEVICE_UUID);
  http.addHeader("X-DEVICE-TOKEN", DEVICE_TOKEN);

  int code = http.GET();

  Serial.print("GET response: ");
  Serial.println(code);

  if (code == 200) {
    String res = http.getString();
    Serial.println("Command: " + res);

    if (res.indexOf("\"time\":true") != -1) {
      isTime = true;
      digitalWrite(RED_LED, LOW);
      digitalWrite(GREEN_LED, HIGH);
      greenStart = millis();
      sendDataToServer();
    }
  }

  http.end();
}

// ===================================================
// HX711 무게 감지
// ===================================================
void checkWeight() {
  unsigned long now = millis();
  if (now - lastWeightReadTime < WEIGHT_READ_INTERVAL) return;

  currentWeight = scale.get_units();
  lastWeightReadTime = now;

  float diff = prevWeight - currentWeight;

  if (diff > 100.0 && !isOpened) {
    isOpened = true;
    openedTime = now;
    Serial.println("⚠️ Weight drop detected!");

    if (!isTime) {
      tone(BUZZER, 1000, 800);
    }
  }

  prevWeight = currentWeight;
}

// ===================================================
// 상태 초기화
// ===================================================
void handleReset() {
  unsigned long now = millis();

  if (isTime && now - greenStart >= GREEN_DURATION) {
    isTime = false;
    digitalWrite(GREEN_LED, LOW);
    digitalWrite(RED_LED, HIGH);
  }

  if (isOpened && now - openedTime >= 10000) {
    isOpened = false;
    noTone(BUZZER);
  }
}

// ===================================================
// LOOP
// ===================================================
void loop() {
  unsigned long now = millis();

  updateBPM();
  checkWeight();
  handleReset();

  if (now - lastGetTime >= GET_INTERVAL) {
    getCommandFromServer();
    lastGetTime = now;
  }

  if (isOpened != lastIsOpened ||
      isTime   != lastIsTime ||
      abs(currentBPM - lastBPM) >= 3) {

    sendDataToServer();

    lastIsOpened = isOpened;
    lastIsTime   = isTime;
    lastBPM      = currentBPM;
  }
}
