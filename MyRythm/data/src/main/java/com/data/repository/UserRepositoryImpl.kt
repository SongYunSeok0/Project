package com.data.repository

import com.data.db.dao.UserDao
import com.data.mapper.user.asDomain
import com.data.mapper.user.asEntity
import com.data.mapper.user.toDomain
import com.data.network.api.UserApi
import com.domain.model.User
import com.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao
) : UserRepository {

    override fun observe(): Flow<User?> =
        dao.observe().map { it?.asDomain() }

    override suspend fun syncUser(): User {
        val dto = api.getMe()
        dao.clear()
        dao.upsert(dto.asEntity())
        return dto.toDomain()
    }

    override suspend fun updateUser(user: User): Boolean {
        return true
    }

    override suspend fun getLocalUser(): User? =
        dao.getOne()?.asDomain()

    override suspend fun fetchRemoteUser(): User? =
        runCatching { api.getMe().toDomain() }.getOrNull()
}
