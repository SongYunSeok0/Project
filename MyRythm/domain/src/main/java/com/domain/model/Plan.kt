package com.domain.model

data class Plan(
    val id: Long,
//    val userId: Long,
    val regihistoryId: Long?,
    val medName: String,
    val takenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Long?,
)
