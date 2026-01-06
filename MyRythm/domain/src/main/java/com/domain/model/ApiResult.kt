package com.domain.model

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val error: DomainError) : ApiResult<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (DomainError) -> Unit): ApiResult<T> {
        if (this is Failure) action(error)
        return this
    }
}