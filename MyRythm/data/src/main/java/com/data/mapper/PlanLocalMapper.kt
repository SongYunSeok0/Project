package com.data.mapper

import com.data.db.entity.PlanEntity
import com.domain.model.Plan

// ---------- DB → Domain ----------
fun PlanEntity.toDomainLocal(): Plan =
    Plan(
        id = id,
        userId = userId,               // Long 그대로
        prescriptionId = prescriptionId,
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
        userId = userId,               // Domain은 Long, Room도 Long → 그대로!
        prescriptionId = prescriptionId,
        medName = medName,
        takenAt = takenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
    )
