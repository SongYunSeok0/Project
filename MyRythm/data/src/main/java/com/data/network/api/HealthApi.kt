// data/network/api/HealthApi.kt
package com.data.network.api

import retrofit2.http.GET

data class LatestHeartRateResponse(
    val bpm: Int?,
    val collected_at: String?
)

interface HealthApi {
    @GET("health/heart/latest/")
    suspend fun getLatestHeartRate(): LatestHeartRateResponse
}
