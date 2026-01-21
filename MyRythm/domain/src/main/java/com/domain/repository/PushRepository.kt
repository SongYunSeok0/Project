package com.domain.repository

import com.domain.model.ApiResult

interface PushRepository {
    suspend fun fetchAndSaveFcmToken(): String?

    suspend fun registerFcmToken(token: String): ApiResult<Unit>
}