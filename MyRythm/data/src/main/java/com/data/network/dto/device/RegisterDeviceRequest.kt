package com.data.network.dto.device

data class RegisterDeviceRequest(
    val uuid: String,
    val token: String,
    val device_name: String
)
