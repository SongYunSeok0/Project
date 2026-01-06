package com.domain.usecase.auth

import com.domain.model.AuthTokens
import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) {
    // 1127 autoLogin 자동로그인설정추가
    suspend operator fun invoke(id: String, pw: String, autoLogin: Boolean): Result<AuthTokens> {
        val loginResult = authRepo.login(id, pw, autoLogin)  // (Result<AuthTokens>)

        if (loginResult.isFailure) {
            return Result.failure(loginResult.exceptionOrNull()!!)
        }

        val tokens = loginResult.getOrNull()!!

        // 2) 프로필 최신화 (실패해도 로그인은 성공 처리해야 함)
        runCatching { userRepo.syncUser() }

        // 3) AuthTokens 반환
        return Result.success(tokens)
    }
}