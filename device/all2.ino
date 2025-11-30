#include <WiFi.h>
#include <HTTPClient.h>
#include "HX711.h"

// ------------------ HX711 ------------------
#define DOUT  4
#define CLK   5
#define CALIBRATION_FACTOR -380.0
HX711 scale(DOUT, CLK);

// ------------------ Heartbeat ------------------
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

// ------------------ LEDs / Buzzer ------------------
#define RED_LED 18
#define GREEN_LED 19
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
unsigned long greenStart = 0;
const unsigned long GREEN_DURATION = 10000;

// --------------filter-----------------------------
int medianBuf[5] = {0};
int medianIndex = 0;
bool fingerPresent = false;

float smoothBPM = 0;
const float BPM_SMOOTH_ALPHA = 0.2;

// ===================================================
// ===== 🔥 median5() 필터 함수 추가 =====
// ===================================================
int median5(int *arr) {
  int buf[5];
  memcpy(buf, arr, sizeof(buf));
  for (int i = 0; i < 4; i++) {
    for (int j = i + 1; j < 5; j++) {
      if (buf[j] < buf[i]) {
        int tmp = buf[i];
        buf[i] = buf[j];
        buf[j] = tmp;
      }
    }
  }
  return buf[2];  // 중앙값
}
// ===================================================


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
// HTTP TASK (비동기)
// ===================================================
void httpTask(void* param) {
  HttpTaskMessage msg;
  for (;;) {
    if (xQueueReceive(httpQueue, &msg, portMAX_DELAY)) {

      // -------- POST --------
      if (msg.doPost) {
        HTTPClient http;
        http.begin(postUrl);
        http.addHeader("Content-Type", "application/json; charset=utf-8");
        http.addHeader("X-DEVICE-UUID", DEVICE_UUID);
        http.addHeader("X-DEVICE-TOKEN", DEVICE_TOKEN);

        int code = http.POST((uint8_t*)msg.body, strlen(msg.body));
        Serial.print("POST code = ");
        Serial.println(code);

        if (code > 0) {
          Serial.println("POST response: " + http.getString());
        }
        http.end();
      }

      // -------- GET --------
      if (msg.doGet) {
        HTTPClient http;
        http.begin(getUrl);
        http.addHeader("X-DEVICE-UUID", DEVICE_UUID);
        http.addHeader("X-DEVICE-TOKEN", DEVICE_TOKEN);

        int code = http.GET();
        Serial.print("GET code = ");
        Serial.println(code);

        if (code == 200) {
          String res = http.getString();
          Serial.println("GET response: " + res);

          if (res.indexOf("\"time\":true") != -1) {
            isTime = true;
            digitalWrite(RED_LED, LOW);
            digitalWrite(GREEN_LED, HIGH);
            greenStart = millis();
          }
        }
        http.end();
      }
    }
  }
}

// ===================================================
// queuePost
// ===================================================
void queuePost() {
  if (millis() - lastPostSend < POST_MIN_INTERVAL) return;

  HttpTaskMessage msg;
  msg.doPost = true;
  msg.doGet  = false;

  snprintf(
    msg.body,
    sizeof(msg.body),
    "{\"device_uuid\":\"%s\",\"isOpened\":%s,\"isTime\":%s,\"bpm\":%d}",
    DEVICE_UUID,
    isOpened ? "true" : "false",
    isTime   ? "true" : "false",
    (int)currentBPM
  );

  Serial.print("QUEUE JSON = ");
  Serial.println(msg.body);

  xQueueSend(httpQueue, &msg, 0);

  lastPostSend = millis();
}

// ===================================================
void queueGet() {
  HttpTaskMessage msg;
  msg.doPost = false;
  msg.doGet  = true;
  xQueueSend(httpQueue, &msg, 0);
}

// ===================================================
// Heartbeat
// ===================================================
void updateBPM() {
  unsigned long now = millis();

  // 1) 부드럽게 ADC 읽기
  int val = readSmooth(SENSOR_PIN);

  // 2) 손가락 감지 (밝기 기준)
  //  - 손 안댐: 0 ~ 200 근처
  //  - 손 댐:  1500 ~ 2500 근처 (너가 준 값 기준)
  fingerPresent = (val > 500);   // 필요하면 400~800 사이에서 조절 가능

  if (!fingerPresent) {
    // 손 안 올리면 BPM은 항상 0으로
    baseline    = 0;
    beats       = 0;
    currentBPM  = 0;
    bpmStartTime = now;
    isAbove     = false;

    // 디버그용
    Serial.printf("NO FINGER  RAW=%d\n", val);
    return;
  }

  // 3) baseline 계산 (손가락 있을 때만)
  if (baseline == 0) {
    baseline = val;   // 처음 한 번 맞추고
  } else {
    // 너무 빨리 따라가지 않게 약간만 섞어줌
    baseline = (baseline * 19 + val) / 20;
  }

  int threshold = baseline + 10;  // 원래 +5 였던거 조금 올림

  // 4) 피크 감지 (예: threshold 넘는 순간 한 번만 카운트)
  //    MIN_GAP 크게 잡아서 중복 카운트 방지
  const unsigned long LOCAL_MIN_GAP = 550; // 0.55초 → 최대 BPM 약 110 근처

  if (!isAbove && val > threshold && (now - lastBeat) > LOCAL_MIN_GAP) {
    isAbove = true;
    lastBeat = now;
    beats++;
    // Serial.println("Beat!");
  } else if (isAbove && val < baseline) {
    // 파형이 다시 baseline 아래로 내려오면 다음 피크 기다림
    isAbove = false;
  }

  // 5) BPM 계산 (윈도우: BPM_INTERVAL = 10000ms = 10초)
  if (now - bpmStartTime >= BPM_INTERVAL) {
    unsigned long window = now - bpmStartTime;
    float instantBPM = 0.0;

    if (window > 0) {
      instantBPM = beats * (60000.0 / window);   // 10초 동안 beat 개수 → BPM
    }

    bpmStartTime = now;
    beats = 0;

    // 약간의 스무딩 (갑자기 튀는 거 방지)
    if (currentBPM == 0) {
      currentBPM = instantBPM;
    } else {
      currentBPM = currentBPM * 0.6f + instantBPM * 0.4f;
    }

    Serial.printf("RAW=%d BASE=%d TH=%d BPM=%.1f (finger=%d)\n",
                  val, baseline, threshold, currentBPM, fingerPresent);
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

  Serial.printf("Weight: %.2f Diff: %.2f\n", currentWeight, diff);

  // 🔥 무게 감소 감지(약 꺼냄)
  if (diff > 100 && !isOpened) {
    isOpened = true;
    openedEvent = true;
    openedTime = now;
    Serial.println("⚠️ Weight drop detected!");

    // 👇 여기 추가된 핵심 로직
    // 약 먹을 시간(isTime = true) 상태에서 약을 꺼냈다면 LED 즉시 빨간불로 전환
    if (isTime) {
      isTime = false;
      digitalWrite(GREEN_LED, LOW);
      digitalWrite(RED_LED, HIGH);
      Serial.println("➡️ Time satisfied! LED -> RED");
    } else {
      // 시간 아닐 때 열면 부저 울리는 기존 기능 유지
      tone(BUZZER, 1000, 800);
    }
  }
}



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
// Setup
// ===================================================
void setup() {
  Serial.begin(115200);
  Serial.println("\n=== PillBox FreeRTOS Async HTTP (SAFE VERSION) ===");

  pinMode(RED_LED, OUTPUT);
  pinMode(GREEN_LED, OUTPUT);
  pinMode(BUZZER, OUTPUT);

  digitalWrite(RED_LED, HIGH);
  digitalWrite(GREEN_LED, LOW);
  digitalWrite(BUZZER, LOW);

  analogReadResolution(12);
  analogSetPinAttenuation(SENSOR_PIN, ADC_11db);

  WiFi.begin(ssid, password);
  Serial.print("WiFi connecting...");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(400);
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
// Loop
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
    abs(currentBPM - lastBPM) >= 25 ||
    (isTime != lastIsTime);

  if (needPost) {
    queuePost();
    lastBPM = currentBPM;
    lastIsTime = isTime;
    openedEvent = false;
  }
}
