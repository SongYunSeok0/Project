package com.data.network.dto.device

import com.domain.model.Device
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "device_name")
    val deviceName: String
)

fun DeviceDto.toDomain(): Device =
    Device(
        id = id,
        name = deviceName
    )