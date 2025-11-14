package com.data.db.entity

import androidx.room.*

@Entity(
    tableName = "plan",
    foreignKeys = [
        ForeignKey(
            entity = PrescriptionEntity::class,
            parentColumns = ["prescriptionId"],
            childColumns = ["prescriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["prescriptionId"])]
)
data class PlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val prescriptionId: Long,
    val medName: String,
    val takenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

