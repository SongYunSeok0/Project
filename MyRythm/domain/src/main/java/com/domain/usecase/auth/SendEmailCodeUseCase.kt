package com.domain.usecase.auth

import com.domain.repository.AuthRepository
import javax.inject.Inject

class SendEmailCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    // Result<Unit>을 반환하여 성공/실패와 실패 이유(Exception)를 전달
    suspend operator fun invoke(email: String, name: String? = null): Result<Boolean> {
        return try {
            val success = repository.sendEmailCode(email, name)
            Result.success(success)
        } catch (e: Exception) {
            // 404 등의 Http Exception이 여기서 잡혀서 전달됨
            Result.failure(e)
        }
    }
}