#pragma once
#include <Preferences.h>

class DeviceConfig {
public:
    static String uuid;
    static String token;
    static String ssid;
    static String pw;

    static void load();
    static void save();
    static bool isRegistered();
};
