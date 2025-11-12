package com.domain.usecase.auth

import com.domain.model.AuthTokens
import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String, pw: String): AuthTokens? {
        val tokens = authRepository.login(id, pw) ?: return null
        userRepository.refreshMe()
        return tokens
    }
}

