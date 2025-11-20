package com.data.repository


import android.util.Log
import com.data.db.dao.UserDao
import com.data.mapper.user.asEntity
import com.data.mapper.user.toDto
import com.data.mapper.user.toProfile
import com.data.network.api.UserApi
import com.domain.model.UserProfile
import com.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao
) : ProfileRepository {

    override suspend fun getProfile(): UserProfile {
        val dto = api.getMe()
        dao.upsert(dto.asEntity())
        return dto.toProfile()
    }


    override suspend fun updateProfile(profile: UserProfile): UserProfile {
        return try {
            val dto = profile.toDto()
            val updatedDto = api.updateProfile(dto)

            dao.upsert(updatedDto.asEntity())
            updatedDto.toProfile()

        } catch (e: Exception) {
            Log.e("ProfileRepo", "🔥 updateProfile 실패: ${e.message}", e)
            throw e
        }
    }
}