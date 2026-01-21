package com.domain.usecase.push

import com.domain.repository.PushRepository
import javax.inject.Inject

class GetFcmTokenUseCase @Inject constructor(
    private val repository: PushRepository
) {
    suspend operator fun invoke(): String? {
        return repository.fetchAndSaveFcmToken()
    }
}