package com.data.network.dto.regihistory

import com.data.network.dto.plan.PlanResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegiHistoryWithPlansResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "user") val user: Long,
    @Json(name = "username") val username: String?,  // 스태프용
    @Json(name = "user_email") val userEmail: String?,  // 스태프용
    @Json(name = "regi_type") val regiType: String,
    @Json(name = "label") val label: String,
    @Json(name = "issued_date") val issuedDate: String?,
    @Json(name = "use_alarm") val useAlarm: Boolean,
    @Json(name = "device") val device: Long?,
    @Json(name = "plans") val plans: List<PlanResponse>,  // Plan 목록 포함
    @Json(name = "plan_count") val planCount: Int  // Plan 개수
)