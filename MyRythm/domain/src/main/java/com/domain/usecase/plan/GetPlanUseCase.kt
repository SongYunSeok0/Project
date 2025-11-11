package com.domain.usecase.plan

import com.domain.model.Plan
import com.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlansUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    operator fun invoke(userId: String): Flow<List<Plan>> =
        repository.observePlans(userId)
}