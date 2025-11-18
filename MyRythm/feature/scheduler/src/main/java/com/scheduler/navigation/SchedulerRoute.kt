package com.scheduler.navigation

import kotlinx.serialization.Serializable

@Serializable
data class SchedulerRoute(val userId: String)

@Serializable
data class RegiRoute(
    val userId: String,
    val prescriptionId: Long
)

@Serializable
data class OcrRoute(
    val userId: String,
    val path: String
)

@Serializable
data class CameraRoute(val userId: String)

