// domain/src/main/java/com/domain/repository/RegiRepository.kt
package com.domain.repository

import com.domain.model.RegiHistory
import com.domain.model.Plan
import kotlinx.coroutines.flow.Flow

interface RegiRepository {
    suspend fun createRegiHistory(
        regiType: String,
        label: String?,
        issuedDate: String?
    ): Long

    // RegiHistory 목록
    fun observeRegiHistories(): Flow<List<RegiHistory>>

    // Plan 여러 개 생성
    suspend fun createPlans(regiHistoryId: Long, list: List<Plan>)

    // Plan 목록
    fun observeAllPlans(userId: Long): Flow<List<Plan>>

    fun observePlans(regiHistoryId: Long): Flow<List<Plan>>
}
