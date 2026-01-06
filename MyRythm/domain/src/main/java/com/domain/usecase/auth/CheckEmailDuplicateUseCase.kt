package com.domain.usecase.auth

import com.domain.model.ApiResult
import com.domain.repository.AuthRepository
import javax.inject.Inject

class CheckEmailDuplicateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): ApiResult<Boolean> {
        return authRepository.checkEmailExists(email)
    }
}
