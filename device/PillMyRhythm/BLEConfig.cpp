#include "BLEConfig.h"
#include "DeviceConfig.h"

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <ArduinoJson.h>

#define SERVICE_UUID        "12345678-1234-1234-1234-1234567890ab"
#define CHARACTERISTIC_UUID "abcd1234-5678-90ab-cdef-1234567890ab"

bool bleConfigDone = false;

class ConfigCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *ch) {
        std::string v = ch->getValue();
        if (v.empty()) return;

        Serial.println("📩 BLE 설정 JSON 수신:");
        Serial.println(v.c_str());

        StaticJsonDocument<256> doc;
        DeserializationError err = deserializeJson(doc, v.c_str());
        if (err) {
            Serial.println("❌ JSON 파싱 실패");
            return;
        }

        // BLE에서 받은 데이터 저장
        DeviceConfig::uuid = doc["uuid"].as<String>();
        DeviceConfig::token = doc["token"].as<String>();
        DeviceConfig::ssid = doc["ssid"].as<String>();
        DeviceConfig::pw = doc["pw"].as<String>();

        DeviceConfig::save();
        Serial.println("✔ BLE 설정 저장 완료!");

        bleConfigDone = true;

        // 등록 완료 → BLE 종료
        BLEDevice::stopAdvertising();
        Serial.println("🛑 BLE Advertising 중단");
    }
};

void startBLEConfig() {
    Serial.println("🔵 BLE 등록 모드 시작");

    BLEDevice::init("PillBox");
    BLEServer *server = BLEDevice::createServer();
    BLEService *service = server->createService(SERVICE_UUID);

    BLECharacteristic *characteristic = service->createCharacteristic(
        CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_WRITE |
        BLECharacteristic::PROPERTY_READ
    );

    characteristic->setCallbacks(new ConfigCallbacks());
    service->start();

    BLEAdvertising *adv = BLEDevice::getAdvertising();
    adv->addServiceUUID(SERVICE_UUID);
    BLEDevice::startAdvertising();

    Serial.println("📢 BLE Advertising ON (앱에서 등록 가능)");
}
