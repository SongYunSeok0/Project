package com.data.repository

import android.util.Log
import com.data.db.dao.UserDao
import com.data.mapper.user.asDomain
import com.data.mapper.user.asEntity
import com.data.mapper.user.toDomain
import com.data.mapper.user.toDto
import com.data.network.api.UserApi
import com.data.network.dto.user.FcmTokenRequestDto
import com.domain.model.SignupRequest
import com.domain.model.User
import com.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao
) : UserRepository {

    // UI 관찰용: Room Entity -> Domain
    override fun observe(): Flow<User?> =
        dao.observe().map { it?.asDomain() }

    // 서버의 /users/me/를 읽어 로컬 DB 갱신
    override suspend fun refreshMe(): User {
        val dto = api.getMe()
        dao.upsert(dto.asEntity())
        return dto.toDomain()
    }

    // 필요 시 구현(서버 업데이트 후 로컬 반영)
    override suspend fun updateUser(user: User): Boolean {
        // 예: api.updateUser(user.toDtoForUpdate()); dao.upsert(updatedDto.asEntity())
        return true
    }

    // ✅ FCM 토큰 등록
    override suspend fun registerFcmToken(token: String) {
        try {
            api.registerFcmToken(FcmTokenRequestDto(fcm_token = token))
        } catch (e: Exception) {
            Log.e("FCM", "registerFcmToken 실패", e)
        }
    }

    // 회원가입
    override suspend fun signup(request: SignupRequest): Boolean {
        return try {
            val res = api.signup(request.toDto())
            if (!res.isSuccessful) {
                Log.e(
                    "Signup",
                    "HTTP ${res.code()} ${res.message()}\n${res.errorBody()?.string()}"
                )
                false
            } else {
                Log.d("Signup", "회원가입 성공: ${res.body()}")
                true
            }
        } catch (e: Exception) {
            Log.e("Signup", "네트워크 예외", e)
            false
        }
    }
}
