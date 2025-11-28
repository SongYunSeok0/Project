package com.data.repository

import com.data.network.api.DeviceApi
import com.domain.model.Device
import com.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val api: DeviceApi
) : DeviceRepository {

    override suspend fun registerDevice(): Device {
        val res = api.registerDevice()

        return Device(
            uuid = res.uuid,
            createdAt = res.created_at
        )
    }
}
