package com.data.network.api

import com.data.network.dto.user.FcmTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PushApi {

    // 예시 URL: /api/push/register-token/
    @POST("push/register-token/")
    suspend fun registerFcmToken(
        @Body body: FcmTokenRequestDto
    )
}
