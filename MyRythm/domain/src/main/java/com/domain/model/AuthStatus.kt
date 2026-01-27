package com.domain.model

data class AuthStatus(
    val isLoggedIn: Boolean,
    val userId: String?,
    val isAutoLoginEnabled: Boolean
)
