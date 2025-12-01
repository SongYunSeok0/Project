package com.data.mapper

import com.data.db.entity.HeartRateEntity
import com.data.network.dto.heart.HeartRateHistoryResponse
import com.domain.model.HeartRateHistory

// 서버 → Entity
fun HeartRateHistoryResponse.toEntity(): HeartRateEntity =
    HeartRateEntity(
        id = id.toLong(),
        bpm = bpm,
        collectedAt = collectedAt
    )

// Entity → Domain
fun HeartRateEntity.toDomain(): HeartRateHistory =
    HeartRateHistory(
        bpm = bpm,
        collectedAt = collectedAt
    )
