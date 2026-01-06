package com.data.repository

import com.data.db.dao.UserDao
import com.data.mapper.user.asDomain
import com.data.mapper.user.asEntity
import com.data.mapper.user.toDomain
import com.data.network.api.UserApi
import com.data.util.mapError
import com.data.util.toDomainException
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

    override suspend fun syncUser(): Result<User> = withContext(io) {
        runCatching {
            val dto = api.getMe()
            dao.clear()
            dao.upsert(dto.asEntity())
            dto.toDomain()
        }.mapError { it.toDomainException() }
    }

    override suspend fun updateUser(user: User): Result<Unit> = withContext(io) {
        runCatching {
            // TODO: 실제 API 호출 구현 필요
            // val res = api.updateUser(user.toDto())
            // if (!res.isSuccessful) {
            //     throw HttpAuthException(res.code(), res.errorBody()?.string())
            // }

            // 현재는 로컬만 업데이트
            dao.upsert(user.asEntity())
            Unit
        }.mapError { it.toDomainException() }
    }

    override suspend fun getLocalUser(): User? =
        dao.getOne()?.asDomain()

    override suspend fun fetchRemoteUser(): Result<User> = withContext(io) {
        runCatching {
            api.getMe().toDomain()
        }.mapError { it.toDomainException() }
    }

    override suspend fun getAllUsers(): Result<List<User>> = withContext(io) {
        runCatching {
            val userDtos = api.getAllUsers()
            userDtos.map { it.asEntity().asDomain() }
        }.mapError { it.toDomainException() }
    }

    override suspend fun getUserById(userId: Long): Result<User> = withContext(io) {
        runCatching {
            val userDto = api.getUserById(userId)
            userDto.asEntity().asDomain()
        }.mapError { it.toDomainException() }
    }

    override suspend fun clearProfile() = withContext(io) {
        dao.clear()
    }
}