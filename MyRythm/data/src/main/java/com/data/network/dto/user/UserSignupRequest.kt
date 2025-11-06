package com.data.network.dto.user

data class UserSignupRequest(
    val id: String,              // 사용자 아이디
    val password: String,        // 비밀번호
    val name: String,            // 이름
    val birth_date: String,      // 생년월일 (예: "2000-05-12")
    val gender: String,          // 성별 (예: "male", "female", "unknown")
    val phone: String            // 전화번호
)
