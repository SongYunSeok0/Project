package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.AuthStatus
import com.domain.repository.AuthRepository

class GetAuthStatusUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): ApiResult<AuthStatus> {
        return authRepository.getAuthStatus()
    }
}

