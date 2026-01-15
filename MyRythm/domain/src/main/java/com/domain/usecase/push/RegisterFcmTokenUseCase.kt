package com.domain.usecase.push

import com.domain.repository.PushRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val repository: PushRepository
) {
    suspend operator fun invoke() {
        val token = repository.fetchAndSaveFcmToken() ?: return
        repository.registerFcmToken(token)
    }
}
