package com.domain.repository

import com.domain.model.AuthTokens
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult

interface AuthRepository {
    suspend fun login(id: String, pw: String): AuthTokens?
    suspend fun refresh(refreshToken: String): AuthTokens?
    suspend fun socialLogin(param: SocialLoginParam): SocialLoginResult

    suspend fun tryRefreshFromLocal(): Boolean

    suspend fun clearTokens()
}
