package com.data.repository


import android.util.Log
import com.data.db.dao.UserDao
import com.data.mapper.user.asEntity
import com.data.mapper.user.toDto
import com.data.mapper.user.toProfile
import com.data.network.api.UserApi
import com.domain.model.UserProfile
import com.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao
) : ProfileRepository {

    private var cachedUserId: Long? = null

    override suspend fun getProfile(): UserProfile {
        val dto = api.getMe()
        cachedUserId = dto.id
        dao.upsert(dto.asEntity())
        return dto.toProfile()
    }

    fun getCachedUserId(): Long {
        return cachedUserId
            ?: throw IllegalStateException("User ID not loaded yet! Call getProfile() first.")
    }

    override suspend fun updateProfile(profile: UserProfile): UserProfile {
        return try {
            val dto = profile.toDto()
            val updatedDto = api.updateProfile(dto)

            dao.upsert(updatedDto.asEntity())
            cachedUserId = updatedDto.id
            updatedDto.toProfile()

        } catch (e: Exception) {
            Log.e("ProfileRepo", "🔥 updateProfile 실패: ${e.message}", e)
            throw e
        }
    }

    override fun observeLocalProfile(): Flow<UserProfile?> {
        return dao.observe().map { entity ->
            entity?.toProfile()
        }
    }

    override suspend fun clearProfile() {
        dao.clear()
        cachedUserId = null
    }
}