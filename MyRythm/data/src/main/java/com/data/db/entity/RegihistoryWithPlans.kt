package com.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RegihistoryWithPlans(
    @Embedded val regihistory: RegihistoryEntity,

    @Relation(
        parentColumn = "regihistoryId",
        entityColumn = "regihistoryId"
    )
    val plans: List<PlanEntity>
)
