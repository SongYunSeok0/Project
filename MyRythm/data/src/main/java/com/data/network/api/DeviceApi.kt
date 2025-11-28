package com.data.network.api

import com.data.network.dto.user.DeviceResponse
import retrofit2.http.POST

interface DeviceApi {
    @POST("iot/device/register/")
    suspend fun registerDevice(): DeviceResponse
}
