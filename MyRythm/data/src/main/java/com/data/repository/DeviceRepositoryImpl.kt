package com.data.repository

import com.data.network.api.DeviceApi
import com.data.network.dto.device.toDomain
import com.domain.model.Device
import com.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val deviceApi: DeviceApi
) : DeviceRepository {

    override suspend fun getMyDevices(): List<Device> {
        val response = deviceApi.getMyDevices() // List<DeviceDto>
        return response.map { it.toDomain() }
    }
}