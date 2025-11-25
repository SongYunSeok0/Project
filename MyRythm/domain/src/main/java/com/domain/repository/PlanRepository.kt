package com.domain.repository

import com.domain.model.Plan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {

    fun observePlans(userId: Long): Flow<List<Plan>>

    suspend fun refresh(userId: Long)

    suspend fun create(
        prescriptionId: Long?,
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Long?,
        useAlarm: Boolean
    )

    suspend fun update(
        userId: Long,
        plan: Plan
    )

    suspend fun delete(userId: Long, planId: Long)
}
