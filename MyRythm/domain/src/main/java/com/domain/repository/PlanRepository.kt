package com.domain.repository

import com.domain.model.Plan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    fun observePlans(userId: Long): Flow<List<Plan>>
    suspend fun refresh(userId: Long)
    suspend fun create(userId: Long, plan: Plan): Long
    suspend fun update(userId: Long, plan: Plan)
    suspend fun delete(userId: Long, planId: Long)
}
