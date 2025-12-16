package com.domain.usecase.mypage

import com.domain.repository.DeviceRepository
import com.domain.repository.UserRepository
import javax.inject.Inject
import com.domain.BLEConnector

class RegisterDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val bleConnector: BLEConnector
) {
    suspend fun execute(
        ssid: String,
        pw: String,
        uuid: String,
        token: String,
        deviceName: String
    ) {
        val userId = userRepository.getLocalUser()?.id
            ?: throw IllegalStateException("로그인이 필요해")

        deviceRepository.registerDevice(
            uuid = uuid,
            token = token,
            name = deviceName
        )

        val connected = bleConnector.scanAndConnect()
        if (!connected) {
            bleConnector.disconnect()
            throw IllegalStateException("BLE 연결 실패")
        }

        val json = """{"uuid":"$uuid","token":"$token","ssid":"$ssid","pw":"$pw"}"""
        val sent = bleConnector.sendConfig(json)
        if (!sent) {
            bleConnector.disconnect()
            throw IllegalStateException("기기 전송 실패")
        }
    }

}
