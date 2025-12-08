// data/src/main/java/com/data/db/entity/PlanEntity.kt
package com.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val regihistoryId: Long?,
    val medName: String,
    val takenAt: Long?,
    val exTakenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Boolean?,
    val takenTime: Long?,
    val useAlarm: Boolean,
    val status: String? = null
)
