package com.data.network.dto.plan

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanUpdateRequest(
    @Json(name = "regihistoryId") val regihistoryId: Long?,
    @Json(name = "medName") val medName: String,
    @Json(name = "takenAt") val takenAt: Long?,
    @Json(name = "exTakenAt") val exTakenAt: Long?,
    @Json(name = "mealTime") val mealTime: String?,
    @Json(name = "note") val note: String?,
    @Json(name = "taken") val taken: Long?,
    @Json(name = "useAlarm") val useAlarm: Boolean
)