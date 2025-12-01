package com.data.mapper

import com.data.db.entity.PlanEntity
import com.domain.model.Plan

// ---------- DB → Domain ----------
fun PlanEntity.toDomainLocal(): Plan =
    Plan(
        id = id,
        regihistoryId = regihistoryId,
        medName = medName,
        takenAt = takenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
        useAlarm = useAlarm
    )

// ---------- Domain → DB ----------
fun Plan.toEntity(): PlanEntity =
    PlanEntity(
        id = id,
        regihistoryId = regihistoryId,
        medName = medName,
        takenAt = takenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
        useAlarm = useAlarm
    )
