package com.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RegiHistoryWithPlansEntity(
    @Embedded val regihistory: RegiHistoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "regihistoryId"
    )
    val plans: List<PlanEntity>
)