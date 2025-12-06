package com.data.repository

import com.data.network.api.DeviceApi
import com.data.network.dto.device.RegisterDeviceRequest
import com.data.network.dto.device.toDomain
import com.domain.model.Device
import com.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val api: DeviceApi,
    private val deviceApi: DeviceApi
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
    override suspend fun getMyDevices(): List<Device> {
        val response = deviceApi.getMyDevices() // List<DeviceDto>
        return response.map { it.toDomain() }
    }
}