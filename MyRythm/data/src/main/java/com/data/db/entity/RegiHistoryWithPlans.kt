package com.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RegiHistoryWithPlans(
    @Embedded val regihistory: RegiHistoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "regihistoryId"
    )
    val plans: List<PlanEntity>
)