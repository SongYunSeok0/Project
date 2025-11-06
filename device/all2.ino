#include <WiFi.h>
#include <HTTPClient.h>
#include "HX711.h"

// ------------------ HX711 설정 ------------------
#define DOUT  4

#define CLK   5
#define CALIBRATION_FACTOR -380.0
HX711 scale(DOUT, CLK);

// ------------------ 맥박 센서 설정 ------------------
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
unsigned long openedTime = 0;

// ------------------ Wi-Fi & 서버 ------------------
const char* ssid = "sesac";
const char* password = "12345678";
const char* postUrl = "http://52.87.174.140:8000/api/alerts/sensor/"; //서버에게 주는곳
const char* getUrl  = "http://52.87.174.140:8000/api/alerts/commands/"; //서버로부터 받는곳

// ------------------ 하드웨어 핀 ------------------
#define RED_LED 14
#define GREEN_LED 27
#define BUZZER 12

// ------------------ 상태 변수 ------------------
bool isOpened = false;
bool isTime = false;
float currentWeight = 0;
float prevWeight = 0;
bool lastIsOpened = false;
bool lastIsTime = false;
float lastBPM = 0;
unsigned long lastWeightReadTime = 0;
const unsigned long WEIGHT_READ_INTERVAL = 500; // HX711 읽기 간격 (ms)
const unsigned long HX711_BLOCK_MS = 80;   

unsigned long lastGetTime = 0;
unsigned long greenStart = 0;
const unsigned long GET_INTERVAL = 10000;
const unsigned long GREEN_DURATION = 10000; // 10초

// ===================================================
// 초기화
// ===================================================
void setup() {
  Serial.begin(115200);
  Serial.println("\n=== Pill Detection + Heartbeat System ===");

  pinMode(RED_LED, OUTPUT);
  pinMode(GREEN_LED, OUTPUT);
  pinMode(BUZZER, OUTPUT);

  digitalWrite(RED_LED, HIGH);
  digitalWrite(GREEN_LED, LOW);
  digitalWrite(BUZZER, LOW);

  analogReadResolution(12); // 값 더 세밀하게 읽기
  analogSetPinAttenuation(SENSOR_PIN, ADC_11db);

  // Wi-Fi 연결
  WiFi.begin(ssid, password);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWi-Fi connected!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  // HX711 초기화
  scale.set_scale(CALIBRATION_FACTOR);
  scale.tare();
  Serial.println("HX711 Ready");

  bpmStartTime = millis();
  prevWeight = scale.get_units();
}

// ===================================================
// 맥박 센서 관련 함수
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

  // HX711 읽은 직후면 스킵
  if (now - lastWeightReadTime < HX711_BLOCK_MS) {
    // optional: Serial.println("PPG skip due to HX711 noise");
    return;
  }

  int val = readSmooth(SENSOR_PIN);
  // baseline 최초 초기화
  if (baseline == 0) baseline = val;
  baseline = (baseline * 19 + val) / 20;
  int threshold = baseline + THRESHOLD_OFFSET;

  if (val > threshold && !isAbove && (now - lastBeat) > MIN_GAP) {
    beats++;
    lastBeat = now;
    isAbove = true;
  } else if (val < baseline) {
    isAbove = false;
  }

  if (now - bpmStartTime >= BPM_INTERVAL) {
    currentBPM = beats * (60000.0 / BPM_INTERVAL);
    Serial.print("BPM: ");
    Serial.println(currentBPM);
    beats = 0;
    bpmStartTime = now;
  }
}


// ===================================================
// 서버 통신
// ===================================================
void sendDataToServer() {
  if (WiFi.status() != WL_CONNECTED) return;

  String jsonData = "{";
  jsonData += "\"isOpened\":" + String(isOpened ? "true" : "false") + ",";
  jsonData += "\"isTime\":" + String(isTime ? "true" : "false") + ",";
  jsonData += "\"Bpm\":" + String((int)currentBPM);
  jsonData += "}";

  Serial.println("Sending data to server: " + jsonData);

  HTTPClient http;
  http.begin(postUrl);
  http.addHeader("Content-Type", "application/json");
  int code = http.POST(jsonData);
  Serial.print("POST Response: ");
  Serial.println(code);

  if (code > 0) {
    String response = http.getString();
    Serial.println("Server says: " + response);
  }

  http.end();
}

// ===================================================
// 서버에서 명령 받기
// ===================================================
void getCommandFromServer() {
  if (WiFi.status() != WL_CONNECTED) return;

  HTTPClient http;
  http.begin(getUrl);
  int code = http.GET();

  if (code == 200) {
    String response = http.getString();
    Serial.println("Command received: " + response);

    if (response.indexOf("\"time\":true") != -1) {
      Serial.println("⏰ Time signal received!");
      isTime = true;
      digitalWrite(RED_LED, LOW);
      digitalWrite(GREEN_LED, HIGH);
      greenStart = millis();

      // 바로 서버 전송
      sendDataToServer();
      lastIsTime = isTime; // 갱신
      lastIsOpened = isOpened;
      lastBPM = currentBPM;
    }
  } else {
    Serial.print("GET failed, code: ");
    Serial.println(code);
  }

  http.end();
}


// ===================================================
// 무게 변화 감지 로직
// ===================================================
void checkWeightChange() {
  unsigned long now = millis();
  if (now - lastWeightReadTime < WEIGHT_READ_INTERVAL) return; // 너무 자주 읽지 않음

  float newWeight = scale.get_units();
  lastWeightReadTime = now; // 읽은 시간 기록

  float diff = prevWeight - newWeight;
  if (diff > 100.0 && !isOpened) { // 100g 이상 줄었을 때
    isOpened = true;
    openedTime = millis(); // 기록
    Serial.println("⚠️ Weight decreased! isOpened = true");

    if (isTime) {
      Serial.println("✅ Time mode: No buzzer");
    } else {
      Serial.println("🚨 Unauthorized open! Buzzer ON");
      tone(BUZZER, 1000, 1000);
    }
  }

  prevWeight = newWeight;
}

// ===================================================
// 상태 업데이트 및 초기화
// ===================================================
void handleLedReset() {
  // 기존 Time 기반 reset
  if (isTime && (millis() - greenStart >= GREEN_DURATION)) {
    Serial.println("🕒 10s passed - Resetting to red LED");
    isTime = false;
    digitalWrite(GREEN_LED, LOW);
    digitalWrite(RED_LED, HIGH);
    noTone(BUZZER);
  }

  // 무게 감소 기반 reset (예: 10초 후)
  if (isOpened && (millis() - openedTime >= 10000)) {
    Serial.println("🕒 10s passed - Resetting isOpened");
    isOpened = false;
    noTone(BUZZER);
  }
}

// ===================================================
// 메인 루프
// ===================================================
void loop() {
  unsigned long now = millis();

  updateBPM();
  checkWeightChange();
  handleLedReset();

  // 10초마다 GET 요청
  if (now - lastGetTime >= GET_INTERVAL) {
    getCommandFromServer();
    lastGetTime = now;
  }

  // 상태 변화 or BPM이 크게 변했을 때만 POST
if (isOpened != lastIsOpened 
    || isTime != lastIsTime 
    || currentBPM >= 60) 
{
  sendDataToServer();

  lastIsOpened = isOpened;
  lastIsTime = isTime;
  lastBPM = currentBPM;
}

}
