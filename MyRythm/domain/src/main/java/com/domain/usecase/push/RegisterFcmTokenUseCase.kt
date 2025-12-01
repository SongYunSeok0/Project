package com.domain.usecase.push

import com.domain.repository.PushRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val repository: PushRepository
) {

    suspend operator fun invoke(token: String) {
        repository.registerFcmToken(token)
    }
}