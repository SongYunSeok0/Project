package com.data.db.entity

import androidx.room.*

@Entity(
    tableName = "plan",
    foreignKeys = [
        ForeignKey(
            entity = RegiHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["regiHistoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["regiHistoryId"])]
)
data class PlanEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val regiHistoryId: Long?,
    val medName: String,
    val takenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Long?,
)

