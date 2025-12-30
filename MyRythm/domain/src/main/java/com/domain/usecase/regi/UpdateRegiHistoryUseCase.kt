package com.domain.usecase.regi

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.model.RegiHistory
import com.domain.repository.RegiRepository
import javax.inject.Inject

class UpdateRegiHistoryUseCase @Inject constructor(
    private val repository: RegiRepository
) {
    suspend operator fun invoke(
        regi: RegiHistory
    ): ApiResult<Unit> {
        return try {
            repository.updateRegiHistory(regi)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(
                DomainError.Unknown(
                    message = e.message ?: "복약 이력 수정 실패"
                )
            )
        }
    }
}
