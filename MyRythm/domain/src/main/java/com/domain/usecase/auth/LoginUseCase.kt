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

        val result = authRepository.login(id, pw, autoLogin)

        if (result is ApiResult.Success) {
            // 계정 전환 대비: 이전 로컬 캐시 제거 후 현재 계정으로 재동기화
            runCatching {
                userRepository.clearProfile()
                userRepository.syncUser()
            }
        }

        return result
    }
}