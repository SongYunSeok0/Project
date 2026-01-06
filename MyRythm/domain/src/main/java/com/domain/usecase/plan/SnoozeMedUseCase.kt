package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.repository.PlanRepository
import javax.inject.Inject

class SnoozeMedUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(planId: Long): ApiResult<Unit> {
        return repository.snoozePlan(planId)
    }
}