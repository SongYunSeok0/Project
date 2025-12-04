package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import javax.inject.Inject

class CheckEmailDuplicateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        return authRepository.checkEmailExists(email)
    }
}