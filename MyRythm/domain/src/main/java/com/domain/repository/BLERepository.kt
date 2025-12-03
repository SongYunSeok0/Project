package com.domain.repository

interface BLERepository {
    suspend fun scanAndConnect(): Boolean
    suspend fun sendConfig(ssid: String, password: String): Boolean
}

