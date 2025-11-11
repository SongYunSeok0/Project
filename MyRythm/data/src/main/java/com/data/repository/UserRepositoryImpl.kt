package com.data.repository

import com.data.mapper.user.toDto
import com.data.network.api.UserApi
import com.domain.model.SignupRequest
import com.domain.model.User
import com.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {

    override suspend fun getUser(userId: String): User {
        // 기존 구현 유지
        throw NotImplementedError()
    }

    override suspend fun refreshUser(userId: String): User {
        // 기존 구현 유지
        throw NotImplementedError()
    }

    override suspend fun updateUser(user: User): Boolean {
        // 기존 구현 유지
        throw NotImplementedError()
    }

    override suspend fun signup(request: SignupRequest): Boolean {
        val res = api.signup(request.toDto())
        return res.isSuccessful
    }
}
