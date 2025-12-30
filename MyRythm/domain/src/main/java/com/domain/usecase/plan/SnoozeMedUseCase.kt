package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.PlanRepository
import javax.inject.Inject

class SnoozeMedUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(planId: Long): ApiResult<Unit> {
        return repository.snoozePlan(planId)
            .fold(
                onSuccess = {
                    ApiResult.Success(Unit)
                },
                onFailure = { e ->
                    ApiResult.Failure(
                        DomainError.Unknown(
                            message = e.message ?: "복용 알림 미루기 실패"
                        )
                    )
                }
            )
    }
}