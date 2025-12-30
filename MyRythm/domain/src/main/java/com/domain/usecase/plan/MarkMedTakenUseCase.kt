package com.domain.usecase.plan

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.PlanRepository
import javax.inject.Inject

class MarkMedTakenUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(planId: Long): ApiResult<Unit> {
        return repository.markAsTaken(planId)
            .fold(
                onSuccess = {
                    ApiResult.Success(Unit)
                },
                onFailure = { e ->
                    ApiResult.Failure(
                        DomainError.Unknown(
                            message = e.message ?: "복용 완료 처리 실패"
                        )
                    )
                }
            )
    }
}