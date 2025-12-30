package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.AuthTokens
import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        id: String,
        pw: String,
        autoLogin: Boolean
    ): ApiResult<AuthTokens> {

        return when (val result =
            authRepository.login(id, pw, autoLogin)) {

            is ApiResult.Success -> {
                runCatching { userRepository.syncUser() }
                result
            }

            is ApiResult.Failure -> result
        }
    }
}

