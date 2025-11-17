package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import javax.inject.Inject

class RefreshTokenUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return repo.tryRefreshFromLocal().getOrDefault(false)
    }
}

