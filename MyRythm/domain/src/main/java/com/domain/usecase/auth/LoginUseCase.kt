package com.domain.usecase.auth

import com.domain.model.AuthTokens
import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(id: String, pw: String): Result<AuthTokens> {
        // 1) 로그인 실행
        val loginResult = authRepo.login(id, pw) // Result<AuthTokens>
        if (loginResult.isFailure) return Result.failure(loginResult.exceptionOrNull()!!)

        val tokens = loginResult.getOrNull()!!

        // 2) 프로필 최신화
        // refreshMe 실행 실패해도 로그인 자체는 성공이므로 감싸줌
        runCatching { userRepo.refreshMe() }

        // 3) AuthTokens 반환
        return Result.success(tokens)
    }
}


