package com.domain.repository

import com.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(): UserProfile

    suspend fun updateProfile(profile: UserProfile): UserProfile

    fun observeLocalProfile(): Flow<UserProfile?>

    suspend fun clearProfile()
}