package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): ApiResult<Unit> {
        if (email.isBlank()) {
            return ApiResult.Failure(DomainError.Validation("이메일을 입력해주세요"))
        }

        if (code.isBlank()) {
            return ApiResult.Failure(DomainError.Validation("인증코드를 입력해주세요"))
        }

        return repository.verifyEmailCode(email, code)
    }
}