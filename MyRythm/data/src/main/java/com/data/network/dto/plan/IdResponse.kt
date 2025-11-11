package com.data.network.dto.plan

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IdResponse(
    @field:Json(name = "id") val id: Long
)
