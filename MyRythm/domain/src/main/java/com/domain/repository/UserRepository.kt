package com.domain.repository

import com.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    // 앱 전체에서 User 상태를 관찰할 때 사용
    fun observe(): Flow<User?>

    // 서버에서 최신 User 가져온 뒤 DB에 반영
    suspend fun syncUser(): User

    // 로컬 User 정보 수정
    suspend fun updateUser(user: User): Boolean

    // 단순히 로컬 DB에서 현재 User 가져오기
    suspend fun getLocalUser(): User?

    // 서버에서 강제로 최신 정보 가져오기만 하는 함수 (옵션)
    suspend fun fetchRemoteUser(): User?

    suspend fun getAllUsers(): Result<List<User>>

    suspend fun getUserById(userId: Long): Result<User>
}
