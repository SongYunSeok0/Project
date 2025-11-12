package com.domain.repository

import com.domain.model.AuthTokens

interface AuthRepository {
    suspend fun login(id: String, pw: String): AuthTokens?
    suspend fun refresh(refreshToken: String): AuthTokens?

    suspend fun tryRefreshFromLocal(): Boolean

    suspend fun clearTokens()
}
