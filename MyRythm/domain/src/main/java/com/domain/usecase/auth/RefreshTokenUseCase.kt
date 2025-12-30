package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.repository.AuthRepository
import javax.inject.Inject

class RefreshTokenUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(): ApiResult<Boolean> {
        return repo.tryRefreshFromLocal()
    }
}


