package com.domain.usecase.regi

import com.domain.model.ApiResult
import com.domain.repository.RegiRepository
import javax.inject.Inject

class DeleteRegiHistoryUseCase @Inject constructor(
    private val repository: RegiRepository
) {
    suspend operator fun invoke(id: Long): ApiResult<Unit> {
        return repository.deleteRegiHistory(id)
    }
}