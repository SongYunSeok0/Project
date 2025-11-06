// PlanEntity.kt
package com.data.db.entity

import androidx.room.*

enum class EPlanType { DISEASE, SUPPLEMENT }
enum class EMealRelation { BEFORE, AFTER, NONE }

@Entity(
    tableName = "plans",
    indices = [
        Index("userId"),
        Index("createdAt")
    ]
)
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val type: EPlanType,
    val diseaseName: String?,
    val supplementName: String?,
    val dosePerDay: Int,
    val mealRelation: EMealRelation?,
    val memo: String?,
    val startDay: Long,
    val endDay: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PlanWithDetails(
    @Embedded val plan: PlanEntity,
    @Relation(parentColumn = "id", entityColumn = "planId")
    val meds: List<PlanMedEntity>,
    @Relation(parentColumn = "id", entityColumn = "planId")
    val times: List<PlanTimeEntity>
)
