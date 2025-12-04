package com.domain.usecase.mypage

import com.domain.model.UserProfile
import com.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<UserProfile?> {
        return repository.observeLocalProfile()
    }
}