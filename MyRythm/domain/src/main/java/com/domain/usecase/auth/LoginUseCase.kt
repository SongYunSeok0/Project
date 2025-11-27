package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) {
    // 1127 autoLogin 자동로그인설정추가
    suspend operator fun invoke(id: String, pw: String, autoLogin: Boolean): Result<Unit> {
        val loginResult = authRepo.login(id, pw, autoLogin)  // 먼저 로그인 시도함 (Result<AuthTokens>)

        if (loginResult.isFailure) {
            return Result.failure(loginResult.exceptionOrNull()!!) // 로그인 실패 시 그대로 실패 반환
        }
        return runCatching {        // 로그인 성공 시 유저 정보 최신화 필요 → refreshMe()
            userRepo.refreshMe()  // suspend
        }
    }
}


