package com.domain.repository

import com.domain.model.User
import com.domain.model.SignupRequest
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observe(): Flow<User?>
    suspend fun refreshMe(): User
    suspend fun updateUser(user: User): Boolean
    suspend fun signup(request: SignupRequest): Boolean

    suspend fun getUser(userId: String): User = refreshMe()
    suspend fun refreshUser(userId: String): User = refreshMe()
}
