package com.domain.usecase.auth

import com.domain.repository.AuthLocalRepository
import javax.inject.Inject

class ClearLocalAuthDataUseCase @Inject constructor(
    private val authLocalRepository: AuthLocalRepository
) {
    operator fun invoke() {
        authLocalRepository.clear()
    }
}
