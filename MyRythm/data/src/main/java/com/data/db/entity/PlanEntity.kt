package com.data.db.entity

import androidx.room.*

@Entity(
    tableName = "plan",
    foreignKeys = [
        ForeignKey(
            entity = RegiHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["regihistoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["regihistoryId"])]
)
data class PlanEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val regihistoryId: Long?,
    val medName: String,
    val takenAt: Long?,
    val exTakenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Long?,
    val useAlarm: Boolean
)

