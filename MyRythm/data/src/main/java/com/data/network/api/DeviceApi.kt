package com.data.network.api

import com.data.network.dto.device.DeviceDto
import retrofit2.http.GET

interface DeviceApi {

    @GET("devices/")
    suspend fun getMyDevices(): List<DeviceDto>
}