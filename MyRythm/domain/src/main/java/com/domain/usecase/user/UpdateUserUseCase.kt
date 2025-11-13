package com.domain.usecase.user

import com.domain.model.User
import com.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(user: User): Boolean = repo.updateUser(user)
}
