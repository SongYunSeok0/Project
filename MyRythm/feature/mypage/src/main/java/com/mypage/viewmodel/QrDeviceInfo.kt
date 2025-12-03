package com.mypage.viewmodel

import org.json.JSONObject

data class QrDeviceInfo(
    val uuid: String,
    val token: String
)

fun parseQrDeviceInfo(raw: String): QrDeviceInfo? {
    return try {
        val json = JSONObject(raw)
        QrDeviceInfo(
            uuid = json.getString("uuid"),
            token = json.getString("token")
        )
    } catch (e: Exception) {
        null
    }
}
