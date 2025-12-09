package com.data.network.dto.plan

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "regihistoryId") val regihistoryId: Long?,
    @Json(name = "regihistory_label") val regihistoryLabel: String?,
    @Json(name = "medName") val medName: String,
    @Json(name = "takenAt") val takenAt: Long?,
    @Json(name = "exTakenAt") val exTakenAt: Long?,
    @Json(name = "mealTime") val mealTime: String?,
    @Json(name = "note") val note: String?,
    @Json(name = "taken") val taken: Boolean?,
    @Json(name = "takenTime") val takenTime: Long?,
    @Json(name = "useAlarm") val useAlarm: Boolean,
    @Json(name = "status") val status: String?
)