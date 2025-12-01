package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): Boolean {
        return repository.verifyEmailCode(email, code)
    }
}