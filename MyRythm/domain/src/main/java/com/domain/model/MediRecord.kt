package com.domain.model

data class MediRecord(
    val id: Long,
    val medicineName: String,
    val takenAt: Long?,
    val mealTime: String?,
    val memo: String?,
    val taken: Boolean?,    // 1=true, 0=false
    val regiLabel: String?
)
