package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        authRepository.clearTokens()
        authRepository.saveAutoLoginEnabled(false)
        return ApiResult.Success(Unit)
    }
}

