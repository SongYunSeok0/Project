package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repo.logout()
    }
}