package com.domain.usecase.plan

import com.domain.model.Plan
import com.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlanByIdUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    operator fun invoke(planId: Long): Flow<Plan?> =
        repository.getPlanById(planId)
}