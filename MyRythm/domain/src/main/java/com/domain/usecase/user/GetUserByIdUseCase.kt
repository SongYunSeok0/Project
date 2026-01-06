package com.domain.usecase.user

import com.domain.model.ApiResult
import com.domain.model.User
import com.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long): ApiResult<User> {
        return userRepository.getUserById(userId)
    }
}