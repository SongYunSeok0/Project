// domain/src/main/java/com/domain/model/DomainError.kt
package com.domain.model

sealed class DomainError(val message: String) {
    data class Network(val msg: String?) : DomainError(msg ?: "인터넷 연결을 확인해주세요")
    data class Server(val code: Int, val msg: String?) : DomainError("서버 오류: HTTP $code - ${msg ?: "unknown"}")
    data class Unknown(val msg: String) : DomainError(msg)
    data object NeedAdditionalInfo : DomainError("추가 정보가 필요합니다")
    data class InvalidToken(val msg: String) : DomainError(msg)
    data class Auth(val msg: String = "인증에 실패했습니다") : DomainError(msg)
    data class Conflict(val msg: String = "이미 존재하는 데이터입니다") : DomainError(msg)
    data class NotFound(val msg: String = "데이터를 찾을 수 없습니다") : DomainError(msg)
    data class Validation(val msg: String) : DomainError(msg)  // ✅ 이거 추가
}