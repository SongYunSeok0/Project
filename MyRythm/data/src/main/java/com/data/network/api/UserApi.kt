package com.data.network.api

import com.data.network.dto.user.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @POST("users/signup/")
    suspend fun signup(@Body user: UserSignupRequest): Response<SignupResponse>

    @POST("token/")
    suspend fun login(@Body request: UserLoginRequest): Response<LoginResponse>

    @POST("token/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    // 본인 정보 조회
    @GET("users/me/")
    suspend fun getMe(): UserDto
}
