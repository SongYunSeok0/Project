package com.domain.usecase.user

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.model.User
import com.domain.repository.UserRepository
import javax.inject.Inject

class RefreshUserUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(userId: String): ApiResult<User> {
        return repo.syncUser().fold(
            onSuccess = { ApiResult.Success(it) },
            onFailure = { ApiResult.Failure(DomainError.Unknown(it.message)) }
        )
    }
}
