package com.domain.repository

import com.domain.model.ApiResult
import com.domain.model.AuthStatus
import com.domain.model.AuthTokens
import com.domain.model.SignupRequest
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult

interface AuthRepository {

    suspend fun sendEmailCode(email: String, name: String? = null): ApiResult<Unit>

    suspend fun verifyEmailCode(email: String, code: String): ApiResult<Unit>

    suspend fun login(id: String, pw: String, autoLogin: Boolean): ApiResult<AuthTokens>

    suspend fun socialLogin(param: SocialLoginParam): ApiResult<SocialLoginResult>

    suspend fun signup(request: SignupRequest): ApiResult<Unit>

    suspend fun resetPassword(email: String, newPassword: String): ApiResult<Unit>

    suspend fun logout(): ApiResult<Unit>

    suspend fun withdrawal(): ApiResult<Unit>

    suspend fun clearTokens(): ApiResult<Unit>

    suspend fun saveAutoLoginEnabled(enabled: Boolean): ApiResult<Unit>

    suspend fun checkEmailExists(email: String): ApiResult<Boolean>

    fun getRawAccessToken(): String?

    suspend fun tryRefreshFromLocal(): ApiResult<Boolean>

    fun getUserId(): Long?

    suspend fun getAuthStatus(): ApiResult<AuthStatus>

}
