package com.domain.model

data class Plan(
    val id: Long,
    val regihistoryId: Long?,
    val medName: String,
    val takenAt: Long?,
    val exTakenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Long?,
    val useAlarm: Boolean
)