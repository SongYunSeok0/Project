package com.domain.repository

import com.domain.model.Plan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {

    fun observePlans(userId: Long): Flow<List<Plan>>

    suspend fun syncPlans(userId: Long)

    suspend fun create(
        regihistoryId: Long?,
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

    // ✅ [수정] userId 파라미터 추가 (refresh를 위해 필요)
    suspend fun createPlansSmart(
        regihistoryId: Long,
        startDate: String,
        duration: Int,
        times: List<String>,
        medName: String
    )

    suspend fun delete(userId: Long, planId: Long)
}
