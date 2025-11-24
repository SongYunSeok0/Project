package com.domain.model

data class StepData(
    val steps: Int,
    val collectedAt: Long
)

data class DailyStep(
    val date: String,
    val steps: Int
)