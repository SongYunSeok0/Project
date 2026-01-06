package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.PlanRepository
import com.domain.model.Plan
import javax.inject.Inject

class UpdatePlanUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(
        userId: Long,
        plan: Plan
    ): ApiResult<Unit> {
        return try {
            repository.update(userId, plan)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(
                DomainError.Unknown(
                    message = e.message ?: "플랜 수정 실패"
                )
            )
        }
    }
}