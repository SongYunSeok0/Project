package com.domain.repository

import com.domain.model.*

interface AuthRepository {
    suspend fun sendEmailCode(email: String, name: String?): Result<Unit>
    suspend fun verifyEmailCode(email: String, code: String): Result<Unit>
    suspend fun login(id: String, pw: String, autoLogin: Boolean): Result<AuthTokens>
    suspend fun saveAutoLoginEnabled(enabled: Boolean)
    suspend fun isAutoLoginEnabled(): Boolean
    suspend fun socialLogin(param: SocialLoginParam): Result<SocialLoginResult>
    suspend fun refresh(refreshToken: String): Result<AuthTokens>
    suspend fun tryRefreshFromLocal(): Result<Boolean>
    suspend fun clearTokens(): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun signup(request: SignupRequest): Result<Unit>
    suspend fun resetPassword(email: String, newPassword: String): Result<Unit>
    suspend fun withdrawal(): Result<Unit>
    fun getUserId(): Long
    suspend fun checkEmailExists(email: String): Result<Boolean>
}