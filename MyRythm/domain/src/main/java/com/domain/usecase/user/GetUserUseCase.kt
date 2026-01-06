package com.domain.usecase.user

import com.domain.model.ApiResult
import com.domain.model.User
import com.domain.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(): ApiResult<User> {
        return repo.syncUser()
    }
}