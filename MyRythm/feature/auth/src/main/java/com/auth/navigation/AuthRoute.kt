package com.auth.navigation

import kotlinx.serialization.Serializable

@Serializable data object AuthGraph
@Serializable data object LoginRoute

//@Serializable data object SignupRoute
// 1112 소셜로그인 부분 추가해서 수정
@Serializable
data class SignupRoute(
    val socialId: String? = null,
    val provider: String? = null
)

@Serializable data object PwdRoute