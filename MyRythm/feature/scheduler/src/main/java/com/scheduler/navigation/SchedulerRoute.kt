package com.scheduler.navigation

import kotlinx.serialization.Serializable

@Serializable object SchedulerRoute

@Serializable
data class RegiRoute(
    val drugNamesCsv: String? = null,
    val times: Int? = null,
    val days: Int? = null
)

@Serializable
data class OcrRoute(val path: String)

@Serializable object CameraRoute
