package com.data.network.dto.user

data class SocialLoginResponse(
    val access: String?,
    val refresh: String?,
    val needAdditionalInfo: Boolean? = false        //신규회원여부, 소셜로그인응답에서만
)

/*
소셜로그인의 경우 서버에서 이러한 응답이 올 수 있음

{
  "access": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "needAdditionalInfo": true
}
 */