#include "DeviceConfig.h"
#include <Preferences.h>

Preferences prefs;

String DeviceConfig::uuid = "";
String DeviceConfig::token = "";
String DeviceConfig::ssid = "";
String DeviceConfig::pw   = "";

void DeviceConfig::load() {
    prefs.begin("device", false);   // read/write 모드로 열기
    uuid = prefs.getString("uuid", "");
    token = prefs.getString("token", "");
    ssid = prefs.getString("ssid", "");
    pw   = prefs.getString("pw", "");
    prefs.end();
}

void DeviceConfig::save() {
    prefs.begin("device", false);
    prefs.putString("uuid", uuid);
    prefs.putString("token", token);
    prefs.putString("ssid", ssid);
    prefs.putString("pw", pw);
    prefs.end();
}

void DeviceConfig::clear() {
    prefs.begin("device", false);
    prefs.clear();   // 모든 값 삭제
    prefs.end();
}

bool DeviceConfig::isRegistered() {
    // 등록 여부는 uuid + token 두 개가 모두 있어야 true
    return uuid.length() > 0 && token.length() > 0;
}

bool DeviceConfig::hasWiFiInfo() {
    // Wi-Fi 연결 가능 여부
    return ssid.length() > 0 && pw.length() > 0;
}
