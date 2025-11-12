package com.domain.usecase.plan

import com.domain.model.Plan
import com.domain.repository.PlanRepository
import javax.inject.Inject

class CreatePlanUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(userId: String, plan: Plan) {
        repository.create(userId, plan)
    }
}