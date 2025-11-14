package com.data.network.dto.plan

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanCreateRequest(
    @Json(name = "prescriptionId") val prescriptionId: Long?,
    @Json(name = "medName") val medName: String,
    @Json(name = "takenAt") val takenAt: Long,
    @Json(name = "mealTime") val mealTime: String?,
    @Json(name = "note") val note: String?,
    @Json(name = "taken") val taken: Long?,
    @Json(name = "createdAt") val createdAt: Long,
    @Json(name = "updatedAt") val updatedAt: Long
)

