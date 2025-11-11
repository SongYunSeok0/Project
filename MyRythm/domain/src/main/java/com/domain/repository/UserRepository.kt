package com.domain.repository
import com.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(): Flow<User?>            // Room 캐시 관찰
    suspend fun refreshUser(uuid: String)     // 서버 → Room 동기화
}
