package com.data.network.api

import com.data.network.dto.user.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    // 회원가입: /api/users/signup/
    @POST("users/signup/")
    suspend fun signup(@Body user: UserSignupRequest): Response<SignupResponse>

    // 로그인: /api/token/  ← email/password 사용
    @POST("token/")
    suspend fun login(@Body request: UserLoginRequest): Response<LoginResponse>

    // 토큰 갱신: /api/token/refresh/
    @POST("token/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    // 사용자 조회(서버 라우트에 맞춰 사용)
    @GET("users/{uuid}")
    suspend fun getUser(@Path("uuid") uuid: String): UserDto

    // 필요 시 본인 정보
    // @GET("users/me/")
    // suspend fun me(): Response<UserDto>
}
