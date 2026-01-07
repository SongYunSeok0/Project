package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.model.DomainError  // 🔥 추가
import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        return try {
            when (val result = authRepository.logout()) {
                is ApiResult.Success -> {
                    authRepository.clearTokens()

                    authRepository.saveAutoLoginEnabled(false)

                    userRepository.clearProfile()

                    ApiResult.Success(Unit)
                }
                is ApiResult.Failure -> {
                    authRepository.clearTokens()
                    authRepository.saveAutoLoginEnabled(false)
                    userRepository.clearProfile()

                    result
                }
            }
        } catch (e: Exception) {
            try {
                authRepository.clearTokens()
                authRepository.saveAutoLoginEnabled(false)
                userRepository.clearProfile()
            } catch (cleanupException: Exception) {
            }

            ApiResult.Failure(DomainError.Unknown(e.message ?: "로그아웃 실패"))
        }
    }
}