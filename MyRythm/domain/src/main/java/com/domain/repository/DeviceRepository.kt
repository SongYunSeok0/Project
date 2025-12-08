package com.domain.repository

import com.domain.model.Device

interface DeviceRepository {
    suspend fun registerDevice(
        uuid: String,
        token: String,
        name: String
    )
    suspend fun getMyDevices(): List<Device>
}