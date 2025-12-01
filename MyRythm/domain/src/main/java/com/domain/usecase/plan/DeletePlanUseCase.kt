package com.domain.usecase.plan

import com.domain.repository.PlanRepository
import javax.inject.Inject

class DeletePlanUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(userId: Long, planId: Long) {
        repository.delete(userId, planId)
    }
}