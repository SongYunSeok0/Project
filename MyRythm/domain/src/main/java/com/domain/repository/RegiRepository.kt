package com.domain.repository

import com.domain.model.RegiHistory
import com.domain.model.Plan
import kotlinx.coroutines.flow.Flow

interface RegiRepository {

    suspend fun createRegiHistory(
        regiType: String,
        label: String?,
        issuedDate: String?,
        useAlarm: Boolean,
        deviceId: String? = null
    ): Long

    fun getRegiHistories(): Flow<List<RegiHistory>>

    suspend fun updateRegiHistory(regi: RegiHistory)

    suspend fun deleteRegiHistory(id: Long)

    suspend fun createPlans(regihistoryId: Long, list: List<Plan>)

    fun observeAllPlans(userId: Long): Flow<List<Plan>>

    fun observePlans(regihistoryId: Long): Flow<List<Plan>>

    suspend fun syncRegiHistories(userId: Long)
}
