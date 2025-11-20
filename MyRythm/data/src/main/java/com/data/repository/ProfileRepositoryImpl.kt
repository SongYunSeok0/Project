package com.data.repository


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


    override suspend fun updateProfile(profile: UserProfile) {
        // 서버 업데이트 요청
        val updatedDto = api.updateProfile(profile.toDto())

        // DB 업데이트
        dao.upsert(updatedDto.asEntity())
    }
}
