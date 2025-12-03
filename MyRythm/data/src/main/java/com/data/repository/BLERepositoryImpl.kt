package com.data.repository

import com.data.device.BLEManager
import com.domain.repository.BLERepository
import javax.inject.Inject

class BLERepositoryImpl @Inject constructor(
    private val bleManager: BLEManager
) : BLERepository {

    override suspend fun scanAndConnect(): Boolean {
        return bleManager.scanAndConnectSuspend()
    }

    override suspend fun sendConfig(ssid: String, password: String): Boolean {
        val json = """
            {"ssid":"$ssid", "password":"$password"}
        """.trimIndent()

        return bleManager.sendConfigSuspend(json)
    }
}
