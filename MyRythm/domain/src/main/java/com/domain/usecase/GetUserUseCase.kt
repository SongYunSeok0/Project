package com.domain.usecase
import com.domain.repository.UserRepository
import com.domain.model.User
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(userId: String): User = repo.getUser(userId)
}