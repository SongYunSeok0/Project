// data/network/api/HealthApi.kt
package com.data.network.api

import com.data.network.dto.health.HeartRateHistoryResponse
import com.data.network.dto.health.LatestHeartRateResponse
import retrofit2.http.GET


interface HealthApi {
    @GET("health/heart/latest/")
    suspend fun getLatestHeartRate(): LatestHeartRateResponse

    @GET("health/heart/")
    suspend fun getHeartHistory(): List<HeartRateHistoryResponse>
}
