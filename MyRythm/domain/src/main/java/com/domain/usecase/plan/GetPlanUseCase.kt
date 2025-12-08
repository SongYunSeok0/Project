// domain/src/main/java/com/domain/usecase/plan/GetPlansUseCase.kt
package com.domain.usecase.plan

import com.domain.model.Plan
import com.domain.repository.RegiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlanUseCase @Inject constructor(
    private val regiRepository: RegiRepository
) {
    operator fun invoke(userId: Long): Flow<List<Plan>> {
        return regiRepository.observeAllPlans(userId)
    }
}
