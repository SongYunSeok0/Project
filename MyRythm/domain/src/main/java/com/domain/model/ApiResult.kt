package com.domain.model

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val error: DomainError) : ApiResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): ApiResult<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Failure -> this
        }
}
