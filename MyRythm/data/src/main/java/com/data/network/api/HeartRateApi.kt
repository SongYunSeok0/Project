// data/network/api/HealthApi.kt
package com.data.network.api

import com.data.network.dto.heart.HeartRateHistoryResponse
import com.data.network.dto.heart.LatestHeartRateResponse
import retrofit2.http.GET


interface HeartRateApi {
    @GET("health/heart/latest/")
    suspend fun getLatestHeartRate(): LatestHeartRateResponse

    @GET("health/heart/")
    suspend fun getHeartHistory(): List<HeartRateHistoryResponse>
}
