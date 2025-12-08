package com.domain.model

import java.time.LocalDate

data class MedicationDelay(
    val date: LocalDate,
    val label: String,
    val scheduledTime: Long,
    val actualTime: Long,
    val delayMinutes: Int,
    val isTaken: Boolean
)