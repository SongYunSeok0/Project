package com.domain.usecase.user

import com.domain.model.User
import com.domain.repository.UserRepository
import javax.inject.Inject

class RefreshUserUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(userId: String): User = repo.refreshUser(userId)
}
