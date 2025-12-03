package com.shared.ui.theme

import androidx.compose.material3.MaterialTheme
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

data class ComponentThemeColor(
    val inquiryCardAnswer : Color,
    val inquiryCardQuestion : Color,
    val chatbotCard : Color,
    val schedulerCard : Color,
    val stepCard : Color,
    val rateCard : Color,
    val timeRemainingCard : Color,
    val mapCard : Color,
    val newsCard : Color,
    val healthInsightCard : Color,
    val mainFeatureCardBorderStroke : Color,
    val appTransparent : Color,
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

//컴포넌트용
val ComponentThemeColors = ComponentThemeColor(
    inquiryCardAnswer = InquiryCardAnswer,
    inquiryCardQuestion = InquiryCardQuestion,
    chatbotCard = ChatbotCard,
    schedulerCard = SchedulerCard,
    stepCard = StepCard,
    rateCard = RateCard,
    timeRemainingCard = TimeRemainingCard,
    mapCard = MapCard,
    newsCard = NewsCard,
    healthInsightCard = HealthInsightCard,
    mainFeatureCardBorderStroke = MainFeatureCardBorderStroke,
    appTransparent = AppTransparent,
)

// Login Local
val LocalLoginThemeColors = staticCompositionLocalOf { LoginThemeColors }
val MaterialTheme.loginTheme: LoginThemeColor
    @Composable
    get() = LocalLoginThemeColors.current

// Auth Local
val LocalAuthThemeColors = staticCompositionLocalOf { AuthThemeColors }
val MaterialTheme.authTheme: AuthThemeColor
    @Composable
    get() = LocalAuthThemeColors.current

val LocalComponentThemeColors = staticCompositionLocalOf { ComponentThemeColors }
val MaterialTheme.componentTheme: ComponentThemeColor
    @Composable
    get() = LocalComponentThemeColors.current