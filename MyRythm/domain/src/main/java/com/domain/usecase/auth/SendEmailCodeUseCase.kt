package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.AuthRepository
import javax.inject.Inject

class SendEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, name: String? = null): ApiResult<Unit> {
        if (email.isBlank()) {
            return ApiResult.Failure(DomainError.EmailSendFailed)
        }
        return repository.sendEmailCode(email, name)
    }
}

