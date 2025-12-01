package com.domain.model

data class HeartRateHistory(
    val bpm: Int,
    val collectedAt: String   // 나중에 예쁘게 포맷해도 됨
)