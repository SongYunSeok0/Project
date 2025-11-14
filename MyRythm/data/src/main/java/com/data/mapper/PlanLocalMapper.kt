package com.data.mapper

import com.data.db.entity.PlanEntity
import com.domain.model.Plan

// ---------- DB → Domain ----------
fun PlanEntity.toDomain(): Plan = Plan(
    id = id,
    userId = userId.toLongOrNull() ?: 0L,
    prescriptionId = prescriptionId,
    medName = medName,
    takenAt = takenAt,
    mealTime = mealTime,
    note = note,
    taken = taken,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ---------- Domain → DB ----------
fun Plan.toEntity(): PlanEntity = PlanEntity(
    id = id,
    userId = userId.toString(),
    prescriptionId = prescriptionId,
    medName = medName,
    takenAt = takenAt,
    mealTime = mealTime,
    note = note,
    taken = taken,
    createdAt = createdAt,
    updatedAt = updatedAt
)
