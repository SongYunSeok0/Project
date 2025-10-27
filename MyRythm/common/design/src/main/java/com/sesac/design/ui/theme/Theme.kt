package com.sesac.design.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// Color, Type, Shape를 통합하여 최종 MaterialTheme 컴포저블을 제공하는 메인 테마 파일.

/*
data class AuthLoginColorScheme(
    val loginbackground: androidx.compose.ui.graphics.Color,
    val loginbutton: androidx.compose.ui.graphics.Color,
    val loginAppName: androidx.compose.ui.graphics.Color,
    val loginText: androidx.compose.ui.graphics.Color,
    val loginSecondaryButton: androidx.compose.ui.graphics.Color,
    val loginOnSecondary: androidx.compose.ui.graphics.Color
)

fun authLoginColorScheme(
    loginbackground: androidx.compose.ui.graphics.Color,
    loginbutton: androidx.compose.ui.graphics.Color,
    loginAppName: androidx.compose.ui.graphics.Color,
    loginText: androidx.compose.ui.graphics.Color,
    loginSecondaryButton: androidx.compose.ui.graphics.Color,
    loginOnSecondary: androidx.compose.ui.graphics.Color
) = AuthLoginColorScheme(
    loginbackground = loginbackground,
    loginbutton = loginbutton,
    loginAppName = loginAppName,
    loginText = loginText,
    loginSecondaryButton = loginSecondaryButton,
    loginOnSecondary = loginOnSecondary
)

private val defaultAuthLoginColors = authLoginColorScheme(
    loginbackground = AuthLoginBackground,
    loginbutton = AuthLoginButton,
    loginAppName = AuthLoginAppName,
    loginText = AuthLoginText,
    loginSecondaryButton = AuthLoginSecondrayButton,
    loginOnSecondary = AuthLoginOnSecondray
)

val LocalAuthLoginColors = staticCompositionLocalOf { defaultAuthLoginColors }

@Composable
fun AuthLoginTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAuthLoginColors provides defaultAuthLoginColors,
        content = content
    )
}

object AuthLoginTheme {
    val colors: AuthLoginColorScheme
        @Composable
        get() = LocalAuthLoginColors.current
}

 */