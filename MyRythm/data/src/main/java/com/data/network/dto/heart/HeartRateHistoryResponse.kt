package com.data.network.dto.heart

import com.squareup.moshi.Json

data class HeartRateHistoryResponse(
    val id: Int,
    val bpm: Int,
    @Json(name = "collected_at")
    val collectedAt: String,
    @Json(name = "created_at")
    val createdAt: String,
    val user: Int
)
