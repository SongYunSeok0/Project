package com.domain.repository

import com.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): UserProfile

    suspend fun updateProfile(profile: UserProfile): UserProfile
}