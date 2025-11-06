package com.data.network.dto.plan

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanUpdateRequest(
    @field:Json(name = "type") val type: String,
    @field:Json(name = "diseaseName") val diseaseName: String?,
    @field:Json(name = "supplementName") val supplementName: String?,
    @field:Json(name = "dosePerDay") val dosePerDay: Int,
    @field:Json(name = "mealRelation") val mealRelation: String?,
    @field:Json(name = "memo") val memo: String?,
    @field:Json(name = "startDay") val startDay: Long,
    @field:Json(name = "endDay") val endDay: Long?,
    @field:Json(name = "meds") val meds: List<String>,
    @field:Json(name = "times") val times: List<String>
)