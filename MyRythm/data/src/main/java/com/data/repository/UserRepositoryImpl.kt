package com.data.repository

import com.data.db.dao.UserDao
import com.data.mapper.user.asDomain
import com.data.mapper.user.asEntity
import com.data.mapper.user.toDomain
import com.data.mapper.user.toUpdateDto
import com.data.network.api.UserApi
import com.data.util.apiResultOf
import com.domain.model.ApiResult
import com.domain.model.User
import com.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {

    override fun observe(): Flow<User?> =
        dao.observe().map { it?.asDomain() }

    override suspend fun syncUser(): ApiResult<User> = withContext(io) {
        apiResultOf {
            val dto = api.getMe()
            dao.clear()
            dao.upsert(dto.asEntity())
            dto.toDomain()
        }
    }

    override suspend fun updateUser(user: User): ApiResult<Unit> = withContext(io) {
        apiResultOf {
            // 1. User → UserUpdateDto 변환
            val updateDto = user.toUpdateDto()

            // 2. 서버에 업데이트 요청 (UserDto 반환)
            val updatedDto = api.updateProfile(updateDto)

            // 3. 서버에서 받은 최신 데이터를 로컬 DB에 저장
            dao.upsert(updatedDto.asEntity())

            Unit
        }
    }

    override suspend fun getLocalUser(): User? =
        dao.getOne()?.asDomain()

    override suspend fun fetchRemoteUser(): ApiResult<User> = withContext(io) {
        apiResultOf {
            api.getMe().toDomain()
        }
    }

    override suspend fun getAllUsers(): ApiResult<List<User>> = withContext(io) {
        apiResultOf {
            val userDtos = api.getAllUsers()
            userDtos.map { it.asEntity().asDomain() }
        }
    }

    override suspend fun getUserById(userId: Long): ApiResult<User> = withContext(io) {
        apiResultOf {
            val userDto = api.getUserById(userId)
            userDto.asEntity().asDomain()
        }
    }

    override suspend fun clearProfile() = withContext(io) {
        dao.clear()
    }
}