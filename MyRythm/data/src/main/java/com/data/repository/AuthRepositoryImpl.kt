package com.data.repository

import com.core.auth.TokenStore
import com.data.mapper.auth.asAuthTokens
import com.data.network.api.UserApi
import com.data.network.dto.user.UserLoginRequest
import com.domain.model.AuthTokens
import com.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun login(id: String, pw: String): AuthTokens? {
        val res = api.login(UserLoginRequest(id, pw))
        val body = res.body() ?: return null
        if (!res.isSuccessful) return null
        val tokens = body.asAuthTokens()
        tokenStore.set(tokens.access, tokens.refresh)
        return tokens
    }

    override suspend fun refresh(refreshToken: String): AuthTokens? {
        // 서버에 refresh 엔드포인트가 없으면 null 유지
        return null
    }

    override suspend fun tryRefreshFromLocal(): Boolean {
        val refresh = tokenStore.current().refresh ?: return false
        val newTokens = refresh(refresh) ?: return false
        tokenStore.set(newTokens.access, newTokens.refresh)
        return true
    }

    override suspend fun clearTokens() {
        tokenStore.clear()
    }
}
