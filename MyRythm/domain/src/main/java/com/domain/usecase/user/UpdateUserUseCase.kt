package com.domain.usecase.user

import com.domain.model.ApiResult
import com.domain.model.User
import com.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(user: User): ApiResult<Unit> {
        return repo.updateUser(user)
    }
}