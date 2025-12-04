package com.domain.usecase.plan

import com.domain.repository.PlanRepository
import javax.inject.Inject

class MarkMedTakenUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(planId: Long): Result<Unit> {
        return repository.markAsTaken(planId)
    }
}