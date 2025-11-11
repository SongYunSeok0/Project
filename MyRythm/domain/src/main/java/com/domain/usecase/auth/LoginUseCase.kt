package com.domain.usecase.auth

import com.domain.model.AuthTokens
import com.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(id: String, pw: String): AuthTokens? =
        repo.login(id, pw)
}
