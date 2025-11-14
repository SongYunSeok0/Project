package com.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PrescriptionWithPlans(
    @Embedded val prescription: PrescriptionEntity,

    @Relation(
        parentColumn = "prescriptionId",
        entityColumn = "prescriptionId"
    )
    val plans: List<PlanEntity>
)
