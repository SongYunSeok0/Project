#include <WiFi.h>
#include <HTTPClient.h>
#include "HX711.h"

// ------------------ HX711 ------------------
#define DOUT  4
#define CLK   5
#define CALIBRATION_FACTOR -380.0
HX711 scale(DOUT, CLK);

// ------------------ Heartbeat Sensor ------------------
const int SENSOR_PIN = 35;
const unsigned long BPM_INTERVAL = 10000; // 10초 윈도우
const unsigned long MIN_GAP = 300;

unsigned long lastBeat = 0;
unsigned long bpmStartTime = 0;
int beats = 0;
int baseline = 0;
bool isAbove = false;
bool fingerPresent = false;
float currentBPM = 0;

int readSmooth(int pin, int samples = 15) {
  long sum = 0;
  for (int i = 0; i < samples; i++) {
    sum += analogRead(pin);
    delay(2);
  }
  return sum / samples;
}

// ------------------ WiFi ------------------
const char* ssid = "sesac";
const char* password = "12345678";

const char* postUrl = "http://192.168.0.154:8000/api/iot/ingest/";
const char* getUrl  = "http://192.168.0.154:8000/api/iot/alerts/commands/";

const char* DEVICE_UUID  = "2cac933d85a51608";
const char* DEVICE_TOKEN = "97211228f1fa705b3b3750f3c7693f3de4086e0b9050aa8df3c1e459e4f1f133";

// ------------------ GREEN LEDs (4 SLOT) ------------------
#define LED1_G 22
#define LED2_G 25
#define LED3_G 27
#define LED4_G 33

int ledPinsG[4] = { LED1_G, LED2_G, LED3_G, LED4_G };
int currentSlot = -1;

// ------------------ BUZZER ------------------
#define BUZZER 12

// ------------------ Weight ------------------
float currentWeight = 0;
float prevWeight = 0;
unsigned long lastWeightReadTime = 0;
const unsigned long WEIGHT_READ_INTERVAL = 500;

bool isOpened = false;
bool openedEvent = false;
unsigned long openedTime = 0;

// ------------------ Time state ------------------
bool isTime = false;

// ------------------ FreeRTOS Queue ------------------
QueueHandle_t httpQueue;

typedef struct {
  bool doPost;
  bool doGet;
  char body[256];
} HttpTaskMessage;

unsigned long lastPostSend = 0;
const unsigned long POST_MIN_INTERVAL = 5000;

unsigned long lastGetSend = 0;
const unsigned long GET_INTERVAL = 10000;

float lastBPM = 0;
bool lastIsTime = false;


// ===================================================
// 4 SLOT LED 업데이트
// ===================================================
void updateSlotLEDs() {
  for (int i = 0; i < 4; i++) {
    if (i == currentSlot) {
      digitalWrite(ledPinsG[i], HIGH);  // 이번에 먹을 칸
    } else {
      digitalWrite(ledPinsG[i], LOW);   // 나머지 OFF
    }
  }
}


// ===================================================
// 심박수 계산(updateBPM)
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

  if (!isAbove && val > threshold && (now - lastBeat) > 550) {
    isAbove = true;
    lastBeat = now;
    beats++;
  } else if (isAbove && val < baseline) {
    isAbove = false;
  }

  if (now - bpmStartTime >= BPM_INTERVAL) {
    float bpm = beats * (60000.0 / (now - bpmStartTime));

    if (currentBPM == 0) currentBPM = bpm;
    else currentBPM = currentBPM * 0.6 + bpm * 0.4;

    bpmStartTime = now;
    beats = 0;

    Serial.printf("[BPM] BASE=%d TH=%d BPM=%.1f finger=%d\n",
                  baseline, threshold, currentBPM, fingerPresent);
  }
}


// ===================================================
// Weight
// ===================================================
void checkWeight() {
  unsigned long now = millis();
  if (now - lastWeightReadTime < WEIGHT_READ_INTERVAL) return;

  currentWeight = scale.get_units();
  lastWeightReadTime = now;

  float diff = prevWeight - currentWeight;
  prevWeight = currentWeight;

  Serial.printf("Weight: %.2f  Diff: %.2f\n", currentWeight, diff);

  if (diff > 20 && !isOpened) {
    isOpened = true;
    openedEvent = true;

    if (!isTime) {
      tone(BUZZER, 1000, 500);
    }
  }
}


// ===================================================
// Reset
// ===================================================
void handleReset() {
  unsigned long now = millis();

  if (isOpened && now - openedTime >= 10000) {
    isOpened = false;
    noTone(BUZZER);
  }
}


// ===================================================
// HTTP TASK
// ===================================================
void httpTask(void* param) {
  HttpTaskMessage msg;
  for (;;) {
    if (xQueueReceive(httpQueue, &msg, portMAX_DELAY)) {

      // ---------------- POST ----------------
      if (msg.doPost) {
        HTTPClient http;
        http.begin(postUrl);
        http.addHeader("Content-Type", "application/json; charset=utf-8");
        http.addHeader("X-DEVICE-UUID", DEVICE_UUID);
        http.addHeader("X-DEVICE-TOKEN", DEVICE_TOKEN);

        http.POST((uint8_t*)msg.body, strlen(msg.body));
        http.end();
      }

      // ---------------- GET ----------------
      if (msg.doGet) {
        HTTPClient http;
        http.begin(getUrl);
        http.addHeader("X-DEVICE-UUID", DEVICE_UUID);
        http.addHeader("X-DEVICE-TOKEN", DEVICE_TOKEN);

        int code = http.GET();
        if (code == 200) {
          String res = http.getString();
          Serial.println("GET response: " + res);

          if (res.indexOf("\"time\":true") != -1) {

            isTime = true;

            currentSlot = (currentSlot + 1) % 4;

            Serial.printf("🔥 time:true → Slot %d ON\n", currentSlot + 1);

            updateSlotLEDs();
          }
        }
        http.end();
      }
    }
  }
}


// ===================================================
// queuePost()
// ===================================================
void queuePost() {
  if (millis() - lastPostSend < POST_MIN_INTERVAL) return;

  HttpTaskMessage msg;
  msg.doPost = true;
  msg.doGet = false;

  snprintf(
    msg.body,
    sizeof(msg.body),
    "{\"device_uuid\":\"%s\",\"isOpened\":%s,\"isTime\":%s,\"bpm\":%.1f}",
    DEVICE_UUID,
    isOpened ? "true" : "false",
    isTime ? "true" : "false",
    currentBPM
  );

  xQueueSend(httpQueue, &msg, 0);
  lastPostSend = millis();
}


// ===================================================
void queueGet() {
  HttpTaskMessage msg;
  msg.doPost = false;
  msg.doGet = true;
  xQueueSend(httpQueue, &msg, 0);
}


// ===================================================
// Setup
// ===================================================
void setup() {
  Serial.begin(115200);
  Serial.println("\n=== PillBox 4-Slot + BPM Version ===");

  // LED 설정
  for (int i = 0; i < 4; i++) {
    pinMode(ledPinsG[i], OUTPUT);
    digitalWrite(ledPinsG[i], LOW);
  }

  pinMode(BUZZER, OUTPUT);
  digitalWrite(BUZZER, LOW);

  analogReadResolution(12);
  analogSetPinAttenuation(SENSOR_PIN, ADC_11db);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println("\nWiFi Connected!");

  scale.set_scale(CALIBRATION_FACTOR);
  scale.tare();
  prevWeight = scale.get_units();

  bpmStartTime = millis();

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
}


// ===================================================
void loop() {
  unsigned long now = millis();

  updateBPM();
  checkWeight();
  handleReset();

  if (now - lastGetSend >= GET_INTERVAL) {
    queueGet();
    lastGetSend = now;
  }

  bool needPost =
    openedEvent ||
    abs(currentBPM - lastBPM) >= 20 ||
    (isTime != lastIsTime);

  if (needPost) {
    queuePost();
    lastBPM = currentBPM;
    lastIsTime = isTime;
    openedEvent = false;
  }
}
