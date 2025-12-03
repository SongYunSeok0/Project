#pragma once
#include <BLEDevice.h>
#include <BLEServer.h>
#include <ArduinoJson.h>
#include "DeviceConfig.h"

void startBLEConfig();
extern bool bleConfigDone;   // BLE 등록 완료 여부
