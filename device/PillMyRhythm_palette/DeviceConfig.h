#ifndef DEVICECONFIG_H
#define DEVICECONFIG_H

#include <Arduino.h>

class DeviceConfig {
public:
    static String uuid;
    static String token;
    static String ssid;
    static String pw;

    static void load();
    static void save();
    static void clear();

    // 등록 여부 = 서버로부터 받은 uuid + token
    static bool isRegistered();

    // Wi-Fi 정보 여부 = ssid + pw
    static bool hasWiFiInfo();
};

#endif
