package com.domain.usecase.mypage

import com.domain.model.UserProfile
import com.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) =
        repository.updateProfile(profile)
}