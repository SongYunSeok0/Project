package com.data.network.api

import com.auth.data.model.SocialLoginRequest
import com.auth.data.model.SocialLoginResponse
import com.data.network.dto.user.UserDto
import com.data.network.dto.user.UserLoginRequest
import com.data.network.dto.user.LoginResponse
import com.data.network.dto.user.RefreshRequest
import com.data.network.dto.user.RefreshResponse
import com.data.network.dto.user.SignupResponse
import com.data.network.dto.user.UserSignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {
//    @POST("users/signup/")
//    suspend fun signup(@Body user: UserSignupRequest): Response<Unit>
//
//    @POST("token/")
//    suspend fun login(@Body request: UserLoginRequest): Response<LoginResponse>

    @GET("users/{uuid}")
    suspend fun getUser(@Path("uuid") uuid: String): UserDto

    @POST("auth/social-login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<SocialLoginResponse>

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