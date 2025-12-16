package com.domain

interface BLEConnector {
    suspend fun scanAndConnect(): Boolean
    suspend fun sendConfig(json: String): Boolean
    fun disconnect()
}
