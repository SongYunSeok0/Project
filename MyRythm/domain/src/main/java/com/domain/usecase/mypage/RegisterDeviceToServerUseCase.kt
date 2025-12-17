package com.domain.usecase.mypage

import com.domain.repository.DeviceRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class RegisterDeviceToServerUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        uuid: String,
        token: String,
        deviceName: String
    ): RegisterDeviceResult {

        val user = userRepository.getLocalUser()
            ?: return RegisterDeviceResult.Error.NotLoggedIn

        if (deviceName.isBlank()) {
            return RegisterDeviceResult.Error.InvalidDeviceName
        }

        return try {
            deviceRepository.registerDevice(uuid, token, deviceName)
            RegisterDeviceResult.Success
        } catch (e: Exception) {
            RegisterDeviceResult.Error.Unknown(
                e.message ?: "서버 등록 실패"
            )
        }
    }
}
