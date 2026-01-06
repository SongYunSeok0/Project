package com.domain.repository

import com.domain.model.ApiResult
import com.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    // 앱 전체에서 User 상태를 관찰할 때 사용
    fun observe(): Flow<User?>

    // 서버에서 최신 User 가져온 뒤 DB에 반영
    suspend fun syncUser(): ApiResult<User>

    // 로컬 User 정보 수정
    suspend fun updateUser(user: User): ApiResult<Unit>

    // 단순히 로컬 DB에서 현재 User 가져오기 (에러 없음)
    suspend fun getLocalUser(): User?

    // 서버에서 강제로 최신 정보 가져오기만 하는 함수
    suspend fun fetchRemoteUser(): ApiResult<User>

    // 모든 사용자 목록 조회
    suspend fun getAllUsers(): ApiResult<List<User>>

    // 특정 사용자 조회
    suspend fun getUserById(userId: Long): ApiResult<User>

    // 로컬 프로필 삭제 (로그아웃 시 사용)
    suspend fun clearProfile()
}