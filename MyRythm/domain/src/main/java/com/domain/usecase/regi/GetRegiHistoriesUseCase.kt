package com.domain.usecase.regi

import com.domain.model.RegiHistory
import com.domain.repository.RegiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRegiHistoriesUseCase @Inject constructor(
    private val repo: RegiRepository
) {
    operator fun invoke(): Flow<List<RegiHistory>> {
        return repo.getRegiHistories()
    }
}