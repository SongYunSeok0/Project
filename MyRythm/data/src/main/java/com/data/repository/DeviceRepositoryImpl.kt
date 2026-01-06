package com.data.repository

import com.data.network.api.DeviceApi
import com.data.network.dto.device.RegisterDeviceRequest
import com.data.network.dto.device.toDomain
import com.data.util.apiResultOf
import com.domain.model.ApiResult
import com.domain.model.Device
import com.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val api: DeviceApi
) : DeviceRepository {

    override suspend fun registerDevice(
        uuid: String,
        token: String,
        name: String
    ): ApiResult<Unit> = apiResultOf {
        api.registerDevice(
            RegisterDeviceRequest(
                uuid = uuid,
                token = token,
                device_name = name
            )
        )
        Unit
    }

    override suspend fun getMyDevices(): ApiResult<List<Device>> = apiResultOf {
        val response = api.getMyDevices()
        response.map { it.toDomain() }
    }
}