package com.domain.model

data class RegiHistory(
    val id: Long,
    val userId: Long,
    val regiType: String,
    val label: String?,
    val issuedDate: String?,
    val useAlarm: Boolean
)
