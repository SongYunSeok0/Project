#include "WiFiManager.h"
#include <WiFi.h>
#include "DeviceConfig.h"

bool connectWiFi() {
    if (DeviceConfig::ssid.length() == 0) {
        Serial.println("⚠ 저장된 Wi-Fi 정보 없음 (BLE 등록 필요)");
        return false;
    }

    Serial.printf("📡 WiFi 연결 시도: SSID=%s\n", DeviceConfig::ssid.c_str());

    WiFi.mode(WIFI_STA);
    WiFi.begin(DeviceConfig::ssid.c_str(), DeviceConfig::pw.c_str());

    int retry = 0;
    while (WiFi.status() != WL_CONNECTED && retry < 20) {
        delay(500);
        Serial.print(".");
        retry++;
    }
    Serial.println("");

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("✔ WiFi 연결 성공!");
        Serial.print("IP: ");
        Serial.println(WiFi.localIP());
        return true;
    } else {
        Serial.println("❌ WiFi 연결 실패");
        return false;
    }
}

bool isWiFiConnected() {
    return WiFi.status() == WL_CONNECTED;
}
