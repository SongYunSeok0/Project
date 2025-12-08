package com.domain.model

data class Plan(
    val id: Long,
    val regihistoryId: Long?,
    val medName: String,
    val takenAt: Long?,
    val exTakenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Boolean?,
    val takenTime: Long?,
    val useAlarm: Boolean,
    val status: PlanStatus = PlanStatus.PENDING
)