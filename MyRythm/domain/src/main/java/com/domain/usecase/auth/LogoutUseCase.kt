package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    // suspend 로 선언
    suspend operator fun invoke() {
        repo.clearTokens()
    }
}
