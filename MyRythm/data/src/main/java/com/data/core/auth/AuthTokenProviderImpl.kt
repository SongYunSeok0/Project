package com.data.core.auth

import com.domain.usecase.auth.AuthTokenProvider
import javax.inject.Inject

class AuthTokenProviderImpl @Inject constructor(
    private val tokenStore: TokenStore
) : AuthTokenProvider {
    override fun getCurrentUserId(): Long? {
        val accessToken = tokenStore.current().access
        return JwtUtils.extractUserId(accessToken)?.toLongOrNull()
    }
}