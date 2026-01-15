package com.domain.usecase.push

import com.domain.repository.PushRepository
import javax.inject.Inject

class RegisterFcmTokenAfterLoginUseCase @Inject constructor(
    private val pushRepository: PushRepository
) {

    suspend operator fun invoke() {
        val token = pushRepository.fetchAndSaveFcmToken()
        token?.let {
            pushRepository.registerFcmToken(it)
        }
    }
}
