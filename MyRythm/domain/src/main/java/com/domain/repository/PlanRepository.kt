package com.domain.repository

import com.domain.model.Plan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    fun observePlans(userId: String): Flow<List<Plan>>
    suspend fun refresh(userId: String)
    suspend fun create(userId: String, plan: Plan): Long
    suspend fun update(userId: String, plan: Plan)  // Unit
    suspend fun delete(userId: String, planId: Long) // Unit
}