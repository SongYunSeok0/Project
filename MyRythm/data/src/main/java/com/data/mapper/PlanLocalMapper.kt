package com.data.mapper

import com.data.db.entity.PlanEntity
import com.domain.model.Plan
import com.domain.model.PlanStatus

// ---------- DB → Domain ----------
fun PlanEntity.toDomainLocal(): Plan =
    Plan(
        id = id,
        regihistoryId = regihistoryId,
        medName = medName,
        takenAt = takenAt,
        exTakenAt = exTakenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
        takenTime = takenTime,
        useAlarm = useAlarm,
        status = PlanStatus.from(status)
    )

// ---------- Domain → DB ----------
fun Plan.toEntity(): PlanEntity =
    PlanEntity(
        id = id,
        regihistoryId = regihistoryId,
        medName = medName,
        takenAt = takenAt,
        exTakenAt = exTakenAt,
        mealTime = mealTime,
        note = note,
        taken = taken,
        takenTime = takenTime,
        useAlarm = useAlarm,
        status = status.value
    )
