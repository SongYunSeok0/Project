package com.scheduler.navigation

import kotlinx.serialization.Serializable

@Serializable
data class SchedulerRoute(
    val userId: String
)


@Serializable
data class RegiRoute(
    val userId: String,
    val drugNames: List<String> = emptyList(),
    val times: Int? = null,
    val days: Int? = null,
    val regihistoryId: Long? = null
)

@Serializable
data class OcrRoute(
    val userId: String,
    val path: String
)

@Serializable
data class CameraRoute(val userId: String)