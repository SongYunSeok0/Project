package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.PlanRepository
import javax.inject.Inject

class RefreshPlansUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(userId: Long): ApiResult<Unit> {
        return try {
            repository.syncPlans(userId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(
                DomainError.Network(
                    message = e.message ?: "플랜 동기화 실패"
                )
            )
        }
    }
}