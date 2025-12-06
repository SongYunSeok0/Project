package com.data.network.dto.regihistory

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegiHistoryRequest(
    @Json(name = "regi_type") val regiType: String,
    @Json(name = "label") val label: String?,
    @Json(name = "issued_date") val issuedDate: String?,
    @Json(name = "useAlarm") val useAlarm: Boolean,
    @Json(name = "device") val device: Long?
)