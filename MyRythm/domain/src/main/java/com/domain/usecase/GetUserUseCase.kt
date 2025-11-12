package com.domain.usecase
import com.domain.repository.UserRepository

class GetUserUseCase(private val repo: UserRepository) {
    operator fun invoke() = repo.observeUser()
}
