package com.domain.usecase.regi

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.model.RegiHistoryWithPlans
import com.domain.repository.RegiRepository
import javax.inject.Inject

class GetUserRegiHistoriesUseCase @Inject constructor(
    private val regiRepository: RegiRepository
) {
    suspend operator fun invoke(
        userId: Long
    ): ApiResult<List<RegiHistoryWithPlans>> {
        return regiRepository.getUserRegiHistories(userId)
            .fold(
                onSuccess = { list ->
                    ApiResult.Success(list)
                },
                onFailure = { e ->
                    ApiResult.Failure(
                        DomainError.Unknown(
                            message = e.message ?: "복약 이력 조회 실패"
                        )
                    )
                }
            )
    }
}