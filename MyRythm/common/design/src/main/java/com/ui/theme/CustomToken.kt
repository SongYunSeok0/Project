package com.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class LoginThemeColor(
    val loginBackground : Color,
    val loginSurface : Color,
    val loginOnFieldHint : Color,
    val loginOnSurface : Color,
    val loginPrimaryButton : Color,
    val loginOnPrimary : Color,
    val loginSecondrayButton : Color,
    val loginOnSecondray : Color,
    val loginAppName : Color,
    val loginTertiary : Color
)

data class AuthThemeColor(
    val authBackground : Color,
    val authSurface : Color,
    val authOnFieldHint : Color,
    val authOnSurface : Color,
    val authPrimaryButton : Color,
    val authPrimaryButtonClick : Color,
    val authOnPrimary : Color,
    val authSecondrayButton : Color,
    val authOnSecondray : Color,
    val authAppName : Color
)

val LoginThemeColors = LoginThemeColor(
    loginBackground = LoginBackground,
    loginSurface = LoginSurface,
    loginOnFieldHint = LoginOnFieldHint,
    loginOnSurface = LoginOnSurface,
    loginPrimaryButton = LoginPrimaryButton,
    loginOnPrimary = LoginOnPrimary,
    loginSecondrayButton = LoginSecondrayButton,
    loginOnSecondray = LoginOnSecondray,
    loginAppName = LoginAppName,
    loginTertiary = LoginTertiary
)
val AuthThemeColors = AuthThemeColor(
    authBackground = AuthBackground,
    authSurface = AuthSurface,
    authOnFieldHint = AuthOnFieldHint,
    authOnSurface = AuthOnSurface,
    authPrimaryButton = AuthPrimaryButton,
    authPrimaryButtonClick = AuthPrimaryButtonClick,
    authOnPrimary = AuthOnPrimary,
    authSecondrayButton = AuthSecondrayButton,
    authOnSecondray = AuthOnSecondray,
    authAppName = AuthAppName
)
// Login Local
val LocalLoginThemeColors = staticCompositionLocalOf { LoginThemeColors }
val androidx.compose.material3.MaterialTheme.loginTheme: LoginThemeColor
    @Composable
    get() = LocalLoginThemeColors.current

// Auth Local
val LocalAuthThemeColors = staticCompositionLocalOf { AuthThemeColors }
val androidx.compose.material3.MaterialTheme.authTheme: AuthThemeColor
    @Composable
    get() = LocalAuthThemeColors.current