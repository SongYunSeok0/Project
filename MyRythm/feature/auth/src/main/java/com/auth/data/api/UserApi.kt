package com.auth.data.api

import com.auth.data.model.UserLoginRequest
import com.auth.data.model.LoginResponse
import com.auth.data.model.SocialLoginRequest
import com.auth.data.model.SocialLoginResponse
import com.auth.data.model.UserSignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("users/signup/")
    suspend fun signup(@Body user: UserSignupRequest): Response<Unit>

    @POST("token/")  // ✅ Django JWT endpoint
    suspend fun login(@Body request: UserLoginRequest): Response<LoginResponse>

    @POST("auth/social-login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<SocialLoginResponse>

}
