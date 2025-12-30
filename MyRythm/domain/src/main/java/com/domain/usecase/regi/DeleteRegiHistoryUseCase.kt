package com.domain.usecase.regi

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.RegiRepository
import javax.inject.Inject

class DeleteRegiHistoryUseCase @Inject constructor(
    private val repository: RegiRepository
) {
    suspend operator fun invoke(id: Long): ApiResult<Unit> {
        return try {
            repository.deleteRegiHistory(id)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(
                DomainError.Unknown(
                    message = e.message ?: "복약 이력 삭제 실패"
                )
            )
        }
    }
}
