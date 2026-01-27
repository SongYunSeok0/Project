package com.domain.usecase.auth

import com.domain.model.AuthStatus
import com.domain.repository.AuthRepository
import javax.inject.Inject

class GetAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthStatus {
        return authRepository.getAuthStatus()
    }
}
