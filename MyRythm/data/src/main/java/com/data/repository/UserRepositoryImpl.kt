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
        return try {
            val res = api.signup(request.toDto())

            if (!res.isSuccessful) {
                val errorBody = res.errorBody()?.string()
                android.util.Log.e("Signup", "HTTP ${res.code()} ${res.message()}\n$errorBody")
                return false
            }

            android.util.Log.d("Signup", "회원가입 성공: ${res.body()}")
            true
        } catch (e: Exception) {
            android.util.Log.e("Signup", "네트워크 예외 발생", e)
            false
        }
    }
}
