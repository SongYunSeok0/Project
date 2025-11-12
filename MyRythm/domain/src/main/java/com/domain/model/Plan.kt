package com.domain.model

enum class PlanType { DISEASE, SUPPLEMENT }
enum class MealRelation { BEFORE, AFTER, NONE }

data class Plan(
    val id: Long = 0L,
    val type: PlanType,
    val diseaseName: String? = null,
    val supplementName: String? = null,
    val dosePerDay: Int,
    val mealRelation: MealRelation? = null,
    val memo: String? = null,
    val startDay: Long,
    val endDay: Long? = null,
    val meds: List<String> = emptyList(),
    val times: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)