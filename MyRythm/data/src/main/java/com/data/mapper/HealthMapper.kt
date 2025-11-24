package com.data.mapper

import com.data.network.dto.health.HeartRateHistoryResponse
import com.domain.model.HeartRateHistory

fun HeartRateHistoryResponse.toDomain(): HeartRateHistory =
    HeartRateHistory(
        bpm = bpm,
        collectedAt = collectedAt // 포맷은 나중에 View 쪽에서 해도 됨
    )
