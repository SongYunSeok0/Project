package com.domain.usecase.regi

import com.domain.model.ApiResult
import com.domain.model.RegiHistory
import com.domain.repository.RegiRepository
import javax.inject.Inject

class UpdateRegiHistoryUseCase @Inject constructor(
    private val repository: RegiRepository
) {
    suspend operator fun invoke(regi: RegiHistory): ApiResult<Unit> {
        return repository.updateRegiHistory(regi)
    }
}