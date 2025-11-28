package com.domain.repository

interface PushRepository {
    suspend fun registerFcmToken(token: String)
    suspend fun fetchAndSaveFcmToken(): String?

}