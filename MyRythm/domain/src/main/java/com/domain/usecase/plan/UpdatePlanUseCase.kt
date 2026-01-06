package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.model.Plan
import com.domain.repository.PlanRepository
import javax.inject.Inject

class UpdatePlanUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(
        userId: Long,
        plan: Plan
    ): ApiResult<Unit> {
        return repository.update(userId, plan)
    }
}