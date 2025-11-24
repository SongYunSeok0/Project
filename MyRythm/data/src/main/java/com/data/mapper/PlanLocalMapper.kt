package com.data.mapper

import com.data.db.entity.PlanEntity
import com.domain.model.Plan

// ---------- DB → Domain ----------
fun PlanEntity.toDomainLocal(): Plan =
    Plan(
        id = id,
        regiHistoryId = regiHistoryId,
        medName = medName,
        takenAt = takenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
    )

// ---------- Domain → DB ----------
fun Plan.toEntity(): PlanEntity =
    PlanEntity(
        id = id,
        regiHistoryId = regiHistoryId,
        medName = medName,
        takenAt = takenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
    )
