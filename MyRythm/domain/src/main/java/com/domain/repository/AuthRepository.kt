package com.domain.repository

import com.domain.model.AuthTokens
import com.domain.model.SignupRequest
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult

interface AuthRepository {
    // 1127 자동로그인 autoLogin: Boolean = false 추가
    suspend fun login(id: String, pw: String, autoLogin: Boolean = false): Result<AuthTokens>
    suspend fun refresh(refreshToken: String): Result<AuthTokens>
    suspend fun socialLogin(param: SocialLoginParam): Result<SocialLoginResult>
    suspend fun tryRefreshFromLocal(): Result<Boolean>
    suspend fun clearTokens(): Result<Unit>
    suspend fun signup(request: SignupRequest): Boolean
    suspend fun sendEmailCode(email: String, name: String? = null): Boolean
    suspend fun verifyEmailCode(email: String, code: String): Boolean
    suspend fun withdrawal(): Boolean

    //1127 자동로그인관련 추가
    suspend fun saveAutoLoginEnabled(enabled: Boolean)
    suspend fun isAutoLoginEnabled(): Boolean

    // 1201 비밀번호잊음창의 휴대폰->이메일 인증 변경중, 비번재설정 추가
    suspend fun resetPassword(email: String, newPassword: String): Boolean

    suspend fun logout(): Result<Unit>

    fun getUserId(): Long

    suspend fun checkEmailExists(email: String): Boolean
}
