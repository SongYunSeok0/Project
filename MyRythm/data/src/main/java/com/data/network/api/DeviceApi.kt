package com.data.network.api

import com.data.network.dto.device.RegisterDeviceRequest
import retrofit2.http.Body
import retrofit2.http.POST
import com.data.network.dto.device.DeviceDto
import retrofit2.http.GET

interface DeviceApi {

    // 기기 등록 (uuid + token + device_name → 모두 body로 보내야 함)
    @POST("iot/device/register/")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    )

    @GET("iot/devices/")
    suspend fun getMyDevices(): List<DeviceDto>
}