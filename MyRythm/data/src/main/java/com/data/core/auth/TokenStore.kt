package com.data.core.auth

import kotlinx.coroutines.flow.Flow

interface TokenStore {
    val tokens: Flow<AuthTokens>
    suspend fun set(access: String?, refresh: String?, persist: Boolean = true)
    suspend fun setAccess(access: String?)
    suspend fun setRefresh(refresh: String?)
    suspend fun clear()

    fun current(): AuthTokens
}
