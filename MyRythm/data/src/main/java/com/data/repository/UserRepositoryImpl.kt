package com.data.repository

import com.data.db.dao.UserDao
import com.data.network.api.UserApi
import com.data.mapper.asDomain
import com.data.mapper.asEntity
import com.domain.model.User
import com.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao
) : UserRepository {

    override fun observeUser(): Flow<User?> =
        dao.observe().map { it?.asDomain() }

    override suspend fun refreshUser(uuid: String) {
        val dto = api.getUser(uuid)
        dao.upsert(dto.asEntity())
    }
}
