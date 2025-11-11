package com.domain.repository

import com.domain.model.User
import com.domain.model.SignupRequest

interface UserRepository {
    suspend fun getUser(userId: String): User
    suspend fun refreshUser(userId: String): User
    suspend fun updateUser(user: User): Boolean
    suspend fun signup(request: SignupRequest): Boolean
}
