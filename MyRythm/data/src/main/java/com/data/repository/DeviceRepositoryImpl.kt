package com.data.repository

import com.data.network.api.DeviceApi
import com.data.network.dto.device.RegisterDeviceRequest
import com.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val api: DeviceApi
) : DeviceRepository {

    override suspend fun registerDevice(
        uuid: String,
        token: String,
        name: String
    ) {
        api.registerDevice(
            RegisterDeviceRequest(
                uuid = uuid,
                token = token,
                device_name = name
            )
        )
    }
}



