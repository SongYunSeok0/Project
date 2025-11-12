package com.domain.usecase.user

import com.domain.model.SignupRequest
import com.domain.repository.UserRepository
import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(request: SignupRequest): Boolean =
        repo.signup(request)
}
