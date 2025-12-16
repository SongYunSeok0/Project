package com.domain.model

import java.time.LocalDate

data class DailyHeartRate(
    val date: LocalDate,
    val measurements: List<Int>
)