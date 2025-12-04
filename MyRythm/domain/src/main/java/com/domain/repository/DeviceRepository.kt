package com.domain.repository

interface DeviceRepository {
    suspend fun registerDevice(
        uuid: String,
        token: String,
        name: String
    )
}
