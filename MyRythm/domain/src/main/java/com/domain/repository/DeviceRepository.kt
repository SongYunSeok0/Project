package com.domain.repository

import com.domain.model.ApiResult
import com.domain.model.Device

interface DeviceRepository {
    suspend fun registerDevice(
        uuid: String,
        token: String,
        name: String
    ): ApiResult<Unit>

    suspend fun getMyDevices(): ApiResult<List<Device>>
}
