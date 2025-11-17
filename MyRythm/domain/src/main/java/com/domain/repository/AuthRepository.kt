package com.domain.repository

import com.domain.model.AuthTokens
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult

interface AuthRepository {
    suspend fun login(id: String, pw: String): Result<AuthTokens>
    suspend fun refresh(refreshToken: String): Result<AuthTokens>
    suspend fun socialLogin(param: SocialLoginParam): Result<SocialLoginResult>
    suspend fun tryRefreshFromLocal(): Result<Boolean>
    suspend fun clearTokens(): Result<Unit>
}

