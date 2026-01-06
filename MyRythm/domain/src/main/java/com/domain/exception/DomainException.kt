package com.domain.exception

sealed class DomainException(message: String) : Exception(message) {
    class NetworkException(message: String = "네트워크 연결에 실패 했습니다.") : DomainException(message)
    class AuthException(message: String = "인증에 실패 했습니다.") : DomainException(message)
    class ConflictException(message: String = "이미 존재하는 데이터입니다.") : DomainException(message)
    class NotFoundException(message: String = "데이터를 찾을 수 없습니다.") : DomainException(message)
    class ValidationException(message: String) : DomainException(message)
    class ServerException(message: String = "서버 오류가 발생했습니다.") : DomainException(message)
    class UnknownException(message: String = "알 수 없는 오류가 발생했습니다.") : DomainException(message)
}