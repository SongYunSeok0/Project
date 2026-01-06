package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.AuthRepository
import javax.inject.Inject

class SendEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, name: String? = null): ApiResult<Unit> {
        // 비즈니스 규칙: 이메일은 필수
        if (email.isBlank()) {
            return ApiResult.Failure(DomainError.Validation("이메일을 입력해주세요"))
        }

        // 비즈니스 규칙: 이메일 형식 검증
        if (!isValidEmail(email)) {
            return ApiResult.Failure(DomainError.Validation("올바른 이메일 형식이 아닙니다"))
        }

        return repository.sendEmailCode(email, name)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }
}