package com.data.util

import com.data.exception.HttpAuthException
import com.domain.model.ApiResult
import com.domain.model.DomainError
import java.io.IOException

fun Exception.toDomainError(): DomainError {
    return when (this) {
        is IOException -> DomainError.Network("인터넷 연결을 확인해주세요")
        is HttpAuthException -> when (code) {
            400 -> DomainError.Validation("잘못된 요청입니다")
            401 -> DomainError.Auth("로그인이 필요합니다")
            404 -> DomainError.NotFound("사용자를 찾을 수 없습니다")
            409 -> DomainError.Conflict("이미 존재하는 이메일입니다")
            500, 502, 503 -> DomainError.Server(code, "서버 오류가 발생했습니다")
            else -> DomainError.Server(code, null)
        }
        else -> DomainError.Unknown(message ?: "알 수 없는 오류가 발생했습니다")
    }
}

inline fun <T> apiResultOf(block: () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: Exception) {
        ApiResult.Failure(e.toDomainError())
    }
}