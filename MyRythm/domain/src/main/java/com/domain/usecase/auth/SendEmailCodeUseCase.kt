package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import javax.inject.Inject

class SendEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        return repository.sendEmailCode(email)
    }
}