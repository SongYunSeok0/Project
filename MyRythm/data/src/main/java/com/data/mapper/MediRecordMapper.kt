package com.data.mapper

import com.data.db.entity.PlanWithRegi
import com.domain.model.MediRecord

fun PlanWithRegi.toDomain(): MediRecord {
    return MediRecord(
        id = plan.id,
        medicineName = plan.medName,
        takenAt = plan.takenAt,
        mealTime = plan.mealTime,
        memo = plan.note,
        taken = plan.taken,
        regiLabel = regi?.label
    )
}
