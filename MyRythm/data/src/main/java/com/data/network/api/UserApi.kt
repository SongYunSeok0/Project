package com.data.network.api

import com.data.network.dto.user.FcmTokenRequestDto
import com.data.network.dto.user.SocialLoginRequest
import com.data.network.dto.user.SocialLoginResponse
import com.data.network.dto.user.UserDto
import com.data.network.dto.user.UserLoginRequest
import com.data.network.dto.user.LoginResponse
import com.data.network.dto.user.RefreshRequest
import com.data.network.dto.user.RefreshResponse
import com.data.network.dto.user.SendCodeRequest
import com.data.network.dto.user.SignupResponse
import com.data.network.dto.user.UserSignupRequest
import com.data.network.dto.user.UserUpdateDto
import com.data.network.dto.user.VerifyCodeRequest
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
//    @POST("users/signup/")
//    suspend fun signup(@Body user: UserSignupRequest): Response<Unit>
//
//    @POST("token/")
//    suspend fun login(@Body request: UserLoginRequest): Response<LoginResponse>

    @GET("users/{uuid}")
    suspend fun getUser(@Path("uuid") uuid: String): UserDto

    @POST("users/social-login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<SocialLoginResponse>

    @POST("/api/auth/send-code/")
    suspend fun sendEmailCode(@Body body: SendCodeRequest): Response<Unit>

    @POST("/api/auth/verify-code/")
    suspend fun verifyEmailCode(@Body body: VerifyCodeRequest): Response<Unit>


    @POST("users/signup/")
    suspend fun signup(@Body user: UserSignupRequest): Response<SignupResponse>

    @POST("token/")
    suspend fun login(@Body request: UserLoginRequest): Response<LoginResponse>

    @POST("token/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    // 본인 정보 조회
    @GET("users/me/")
    suspend fun getMe(): UserDto

    @POST("users/fcm/")
    suspend fun registerFcmToken(@Body body: FcmTokenRequestDto): Response<Unit>

    @PATCH("users/me/")
    suspend fun updateProfile(@Body dto: UserUpdateDto): UserDto

    @DELETE("users/withdrawal/")
    suspend fun deleteAccount(): Response<Unit>

}