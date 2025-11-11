package com.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plan_meds",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class PlanMedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val planId: Long,     // 상위 PlanEntity의 id (FK)
    val name: String      // 약 이름 (예: "타이레놀정")
)
