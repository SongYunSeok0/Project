package com.domain.usecase.mypage

import javax.inject.Inject

class RegisterDeviceUseCase @Inject constructor(
    private val registerDeviceToServer: RegisterDeviceToServerUseCase,
    private val sendDeviceConfigByBle: SendDeviceConfigByBleUseCase
) {
    suspend operator fun invoke(
        ssid: String,
        pw: String,
        uuid: String,
        token: String,
        deviceName: String
    ): RegisterDeviceResult {

        val serverResult =
            registerDeviceToServer(uuid, token, deviceName)

        if (serverResult is RegisterDeviceResult.Error) {
            return serverResult
        }

        return sendDeviceConfigByBle(
            ssid = ssid,
            pw = pw,
            uuid = uuid,
            token = token
        )
    }
}



