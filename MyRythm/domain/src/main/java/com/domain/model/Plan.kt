package com.domain.model

data class Plan(
    val id: Long,
    val regiHistoryId: Long?,
    val medName: String,
    val takenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Long?,
)
