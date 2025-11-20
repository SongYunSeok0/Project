package com.domain.usecase.user

import com.domain.model.SignupRequest
import com.domain.repository.AuthRepository
import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(request: SignupRequest): Boolean =
        repo.signup(request)
}
