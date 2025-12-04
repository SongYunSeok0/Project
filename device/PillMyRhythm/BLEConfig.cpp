#include "BLEConfig.h"
#include "DeviceConfig.h"

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <ArduinoJson.h>

#define SERVICE_UUID        "12345678-1234-1234-1234-1234567890ab"
#define CHARACTERISTIC_UUID "abcd1234-5678-90ab-cdef-1234567890ab"

bool bleConfigDone = false;

class ServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) override {
        Serial.println("🔗 BLE Connected!");
    }
    void onDisconnect(BLEServer* pServer) override {
        Serial.println("❌ BLE Disconnected!");
        delay(100);
        BLEDevice::startAdvertising();
        Serial.println("📢 Advertising restarted (disconnect)");
    }
};

class ConfigCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *ch) override {

        String v = ch->getValue();
        if (v.length() == 0) return;

        Serial.println("📩 BLE 설정 JSON 수신:");
        Serial.println(v);

        StaticJsonDocument<256> doc;
        DeserializationError err = deserializeJson(doc, v);
        if (err) {
            Serial.println("❌ JSON 파싱 실패");
            return;
        }

        DeviceConfig::uuid = doc["uuid"].as<String>();
        DeviceConfig::token = doc["token"].as<String>();
        DeviceConfig::ssid  = doc["ssid"].as<String>();
        DeviceConfig::pw    = doc["pw"].as<String>();

        DeviceConfig::save();
        Serial.println("✔ BLE 설정 저장 완료!");

        bleConfigDone = true;
    }
};

void startBLEConfig() {
    Serial.println("🔵 BLE 등록 모드 시작");

    static BLEServer* server = nullptr;
    static BLEService* service = nullptr;
    static BLECharacteristic* characteristic = nullptr;

    BLEDevice::init("PillBox");

    server = BLEDevice::createServer();
    server->setCallbacks(new ServerCallbacks());

    service = server->createService(SERVICE_UUID);

    characteristic = service->createCharacteristic(
        CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_WRITE |
        BLECharacteristic::PROPERTY_READ
    );

    characteristic->setCallbacks(new ConfigCallbacks());

    service->start();

    BLEAdvertising *adv = BLEDevice::getAdvertising();
    adv->addServiceUUID(SERVICE_UUID);
    adv->setScanResponse(true);      // 있어도 상관 없음
    adv->setMinPreferred(0x06);
    adv->setMinPreferred(0x12);

    BLEDevice::startAdvertising();

    Serial.println("📢 BLE Advertising ON (PillBox 등록 가능)");
}

