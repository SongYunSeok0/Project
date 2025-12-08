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
        delay(200);
        BLEDevice::startAdvertising();
    }
};

class ConfigCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *ch) override {

        String v = ch->getValue();
        if (v.length() == 0) return;

        Serial.println("📩 BLE 설정 JSON 원본 수신:");
        Serial.println(v);

        StaticJsonDocument<256> doc;
        auto err = deserializeJson(doc, v);
        if (err) {
            Serial.print("❌ JSON 파싱 실패: ");
            Serial.println(err.f_str());
            return;
        }

        // ⭐ 파싱된 값 상세 출력
        Serial.println("🔍 파싱된 BLE 설정 내용:");
        Serial.print("  uuid  = "); Serial.println(doc["uuid"].as<String>());
        Serial.print("  token = "); Serial.println(doc["token"].as<String>());
        Serial.print("  ssid  = "); Serial.println(doc["ssid"].as<String>());
        Serial.print("  pw    = "); Serial.println(doc["pw"].as<String>());

        // ⭐ 기존 등록 정보 삭제
        Serial.println("🧹 기존 DeviceConfig 초기화");
        DeviceConfig::clear();

        // ⭐ 새로운 값 설정
        DeviceConfig::uuid  = doc["uuid"].as<String>();
        DeviceConfig::token = doc["token"].as<String>();
        DeviceConfig::ssid  = doc["ssid"].as<String>();
        DeviceConfig::pw    = doc["pw"].as<String>();

        DeviceConfig::save();

        Serial.println("💾 DeviceConfig 저장 완료!");
        Serial.println("🟢 BLE 설정 완료 → 재부팅 준비됨");

        bleConfigDone = true;
    }
};


void startBLEConfig() {
    Serial.println("🔵 BLE 등록 모드 시작");

    BLEDevice::init("PillBox");
    BLEDevice::setMTU(256);
    BLEServer *server = BLEDevice::createServer();
    server->setCallbacks(new ServerCallbacks());

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
    adv->setScanResponse(true);

    BLEDevice::startAdvertising();

    Serial.println("📢 BLE Advertising ON (PillBox 등록 가능)");
}
