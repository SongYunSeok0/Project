package com.data.network.dto.step

data class StepCountRequest(
    val steps: Int,
    val collected_at: Long
)

data class DailyStepRequest(
    val date: String,
    val steps: Int
)
