package com.domain.usecase.mypage

import com.domain.BLEConnector
import javax.inject.Inject

class SendDeviceConfigByBleUseCase @Inject constructor(
    private val bleConnector: BLEConnector
) {
    suspend operator fun invoke(
        ssid: String,
        pw: String,
        uuid: String,
        token: String
    ): RegisterDeviceResult {

        if (!bleConnector.scanAndConnect()) {
            bleConnector.disconnect()
            return RegisterDeviceResult.Error.BleConnectFailed
        }

        val json =
            """{"uuid":"$uuid","token":"$token","ssid":"$ssid","pw":"$pw"}"""

        if (!bleConnector.sendConfig(json)) {
            bleConnector.disconnect()
            return RegisterDeviceResult.Error.BleSendFailed
        }

        return RegisterDeviceResult.Success
    }
}
