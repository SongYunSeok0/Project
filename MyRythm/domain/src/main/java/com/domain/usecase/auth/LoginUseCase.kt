package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) {
    // 1126 autoLogin 자동로그인설정추가
    suspend operator fun invoke(id: String, pw: String, autoLogin: Boolean): Result<Unit> {
        // 1) 로그인 시도 (Result<AuthTokens>)
        val loginResult = authRepo.login(id, pw, autoLogin)

        if (loginResult.isFailure) {
            // 로그인 실패 → 그대로 실패 반환
            return Result.failure(loginResult.exceptionOrNull()!!)
        }

        // 2) 유저 정보 최신화 필요 → refreshMe()
        return runCatching {
            userRepo.refreshMe()  // suspend
        }
    }
}


