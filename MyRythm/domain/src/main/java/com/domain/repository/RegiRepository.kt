package com.domain.repository

import com.domain.model.ApiResult
import com.domain.model.RegiHistory
import com.domain.model.Plan
import com.domain.model.RegiHistoryWithPlans
import kotlinx.coroutines.flow.Flow

interface RegiRepository {

    suspend fun createRegiHistory(
        regiType: String,
        label: String?,
        issuedDate: String?,
        useAlarm: Boolean,
        device: Long?
    ): ApiResult<Long>

    fun getRegiHistories(): Flow<List<RegiHistory>>

    suspend fun updateRegiHistory(regi: RegiHistory): ApiResult<Unit>

    suspend fun deleteRegiHistory(id: Long): ApiResult<Unit>

    suspend fun createPlans(regihistoryId: Long?, list: List<Plan>): ApiResult<Unit>

    fun observeAllPlans(userId: Long): Flow<List<Plan>>

    fun observePlans(regihistoryId: Long): Flow<List<Plan>>

    suspend fun syncRegiHistories(userId: Long): ApiResult<Unit>

    suspend fun getUserRegiHistories(userId: Long): ApiResult<List<RegiHistoryWithPlans>>
    suspend fun getAllRegiHistories(): ApiResult<List<RegiHistoryWithPlans>>
    suspend fun getUserPlans(userId: Long): ApiResult<List<Plan>>
}