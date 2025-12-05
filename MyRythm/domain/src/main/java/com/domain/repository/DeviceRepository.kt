package com.domain.repository

import com.domain.model.Device

interface DeviceRepository {
    suspend fun getMyDevices(): List<Device>
}