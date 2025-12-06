#include "DeviceConfig.h"
#include <Preferences.h>

Preferences prefs;

String DeviceConfig::uuid = "";
String DeviceConfig::token = "";
String DeviceConfig::ssid = "";
String DeviceConfig::pw   = "";

void DeviceConfig::load() {
    prefs.begin("device", true);
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
    prefs.clear();
    prefs.end();
}

bool DeviceConfig::isRegistered() {
    return uuid.length() > 0 && ssid.length() > 0;
}
