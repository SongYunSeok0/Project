package com.domain.usecase.push

import com.domain.repository.UserRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(token: String) {
        userRepository.registerFcmToken(token)
    }
}
