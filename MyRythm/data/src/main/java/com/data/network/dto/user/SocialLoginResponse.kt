package com.data.network.dto.user

data class SocialLoginResponse(
    val access: String?,
    val refresh: String?,
    val needAdditionalInfo: Boolean? = false,        //신규회원여부, 소셜로그인응답에서만
    val provider: String? = null,
    val socialId: String? = null
)

/*
val needAdditionalInfo: Boolean? = false 는 기존 소셜 유저가 로그인 성공 시
                                    true 는 신규 소셜 유저니까 여기서 프로바이더+소셜아이디 받아옴
 */