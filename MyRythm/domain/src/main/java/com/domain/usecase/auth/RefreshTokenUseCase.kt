package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import javax.inject.Inject

class RefreshTokenUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return repo.tryRefreshFromLocal()
    }
}