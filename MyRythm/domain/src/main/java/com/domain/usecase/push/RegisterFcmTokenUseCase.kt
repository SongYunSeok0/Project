package com.domain.usecase.push

import com.domain.model.ApiResult
import com.domain.repository.PushRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val repository: PushRepository
) {
    suspend operator fun invoke(token: String): ApiResult<Unit> {
        return repository.registerFcmToken(token)
    }
}