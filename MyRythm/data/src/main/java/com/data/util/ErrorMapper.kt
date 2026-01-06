package com.data.util

// import com.data.repository.HttpAuthException
import com.domain.exception.DomainException
import java.io.IOException

fun Throwable.toDomainException(): DomainException {
    return when (this) {
        is IOException -> DomainException.NetworkException("인터넷 연결을 확인해주세요")
//        is HttpAuthException -> when (code) {
//            400 -> DomainException.ValidationException("잘못된 요청입니다")
//            401 -> DomainException.ValidationException("로그인이 필요합니다")
//            404 -> DomainException.ValidationException("사용자를 찾을 수 없습니다")
//            409 -> DomainException.ValidationException("이미 존재하는 이메일입니다")
//            500, 502, 503 -> DomainException.ValidationException("서버 오류가 발생했습니다")
//            else -> DomainException.ServerException("서버 오류: Http $code")
//        } //파일이 없어서 주석처리함
        else -> DomainException.UnknownException(message ?: "알 수 없는 오류가 발생했습니다")
    }
}

inline fun <R> Result<R>.mapError(transfrom: (Throwable) -> Throwable): Result<R> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transfrom(it)) }
    )
}
