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
    @PrimaryKey(autoGenerate = false) // 🔥 서버 ID 사용
    val id: Long,

    val userId: Long, // 🔥 Long 으로 변경

    val prescriptionId: Long?, // 🔥 nullable

    val medName: String,
    val takenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

