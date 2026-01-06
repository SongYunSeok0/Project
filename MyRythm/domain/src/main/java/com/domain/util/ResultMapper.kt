package com.domain.util

import com.domain.model.ApiResult

inline fun <T> apiResultOf(block: () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: Exception) {
        ApiResult.Failure(com.domain.model.DomainError.Unknown(e.message ?: "알 수 없는 오류"))
    }
}