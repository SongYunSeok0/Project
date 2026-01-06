package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.repository.AuthRepository
import javax.inject.Inject

class WithdrawalUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        return repository.withdrawal()
    }
}
