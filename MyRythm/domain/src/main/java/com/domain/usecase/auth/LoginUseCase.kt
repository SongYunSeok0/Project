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

// 1127 11:278 merge seok into yun
/* yun 기존코드
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
 */

/* seok 기존코드
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



 */
