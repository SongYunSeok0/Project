package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): ApiResult<Unit> {
        if (email.isBlank() || code.isBlank()) {
            return ApiResult.Failure(DomainError.VerifyCodeFailed)
        }
        return repository.verifyEmailCode(email, code)
    }
}

