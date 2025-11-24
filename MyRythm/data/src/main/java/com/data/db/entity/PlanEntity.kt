package com.data.db.entity

import androidx.room.*

@Entity(
    tableName = "plan",
    foreignKeys = [
        ForeignKey(
            entity = RegihistoryEntity::class,
            parentColumns = ["regihistoryId"],
            childColumns = ["regihistoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["regihistoryId"])]
)
data class PlanEntity(
    @PrimaryKey(autoGenerate = false) // 🔥 서버 ID 사용
    val id: Long,

//    val userId: Long, // 🔥 Long 으로 변경

    val regihistoryId: Long?, // 🔥 nullable

    val medName: String,
    val takenAt: Long?,
    val mealTime: String?,
    val note: String?,
    val taken: Long?,
)

