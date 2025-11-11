package com.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plan_times",
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
data class PlanTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val planId: Long,     // 상위 PlanEntity의 id (FK)
    val orderIndex: Int,  // 순서 (0,1,2,…)
    val hhmm: String      // "08:00" 같은 시각 문자열
)
