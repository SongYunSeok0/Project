package com.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RegiHistoryWithPlans(
    @Embedded val regiHistory: RegiHistoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "regiHistoryId"
    )
    val plans: List<PlanEntity>
)