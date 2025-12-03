package com.domain.usecase.plan

import com.domain.repository.PlanRepository
import javax.inject.Inject

class RefreshPlansUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(userId: Long) {
        repository.syncPlans(userId)
    }
}