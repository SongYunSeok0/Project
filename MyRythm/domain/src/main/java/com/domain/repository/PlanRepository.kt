package com.domain.repository

import com.domain.model.ApiResult
import com.domain.model.Plan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {

    fun observePlans(userId: Long): Flow<List<Plan>>

    fun getPlanById(planId: Long): Flow<Plan?>

    suspend fun syncPlans(userId: Long): ApiResult<Unit>

    suspend fun create(
        regihistoryId: Long?,
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Boolean?,
        useAlarm: Boolean
    ): ApiResult<Unit>

    suspend fun update(
        userId: Long,
        plan: Plan
    ): ApiResult<Unit>

    suspend fun createPlansSmart(
        regihistoryId: Long,
        startDate: String,
        duration: Int,
        times: List<String>,
        medName: String
    ): ApiResult<Unit>

    suspend fun delete(userId: Long, planId: Long): ApiResult<Unit>

    suspend fun markAsTaken(planId: Long): ApiResult<Unit>

    suspend fun snoozePlan(planId: Long): ApiResult<Unit>

    suspend fun getRecentTakenPlans(userId: Long, days: Int = 7): ApiResult<List<Plan>>
}