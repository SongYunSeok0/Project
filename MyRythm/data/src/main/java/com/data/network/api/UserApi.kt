package com.data.network.api

import com.data.network.dto.user.UserDto
import com.data.network.dto.user.UserLoginRequest
import com.data.network.dto.user.LoginResponse
import com.data.network.dto.user.UserSignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {
    @POST("users/signup/")
    suspend fun signup(@Body user: UserSignupRequest): Response<Unit>

    @POST("token/")
    suspend fun login(@Body request: UserLoginRequest): Response<LoginResponse>

    @GET("users/{uuid}")
    suspend fun getUser(@Path("uuid") uuid: String): UserDto
}