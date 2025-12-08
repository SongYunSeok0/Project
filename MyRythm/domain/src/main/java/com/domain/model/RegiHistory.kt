package com.domain.model

data class RegiHistory(
    val id: Long,
    val userId: Long,
    val regiType: String,
    val label: String,
    val issuedDate: String?,
    val useAlarm: Boolean,
    val device: Long?
)

data class RegiHistoryWithPlans(
    val id: Long,
    val userId: Long,
    val username: String?,  // 스태프용
    val userEmail: String?,  // 스태프용
    val regiType: String,
    val label: String,
    val issuedDate: String?,
    val useAlarm: Boolean,
    val device: Long?,
    val plans: List<Plan>,  // Plan 목록
    val planCount: Int  // Plan 개수
)