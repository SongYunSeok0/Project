package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.PlanRepository
import javax.inject.Inject

class DeletePlanUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(
        userId: Long,
        planId: Long
    ): ApiResult<Unit> {
        return try {
            repository.delete(userId, planId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(
                DomainError.Unknown(
                    message = e.message ?: "플랜 삭제 실패"
                )
            )
        }
    }
}