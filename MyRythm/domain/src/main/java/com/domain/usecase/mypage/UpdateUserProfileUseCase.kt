package com.domain.usecase.mypage

import com.domain.model.UserProfile
import com.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(profile: UserProfile) =
        repository.updateProfile(profile)
}