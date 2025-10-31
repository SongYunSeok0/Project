package com.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Color, Type, Shape를 통합하여 최종 MaterialTheme 컴포저블을 제공하는 메인 테마 파일.

// Material3 기본 테마
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    surface = Surface,
    onSurface = OnSurface,
    background = BackGround,
    onBackground = BasicBlack,

    // 오류 팝업
    error = Color.Red,
    onError = BasicWhite
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    // 라이트 테마 ColorScheme 사용
    val colorScheme = LightColorScheme

    // 상태 표시줄(Status Bar) 색상 및 아이콘 밝기 설정
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 상태 표시줄 배경색을 앱의 배경색으로 설정
            window.statusBarColor = colorScheme.background.toArgb()

            // 상태 표시줄 콘텐츠(아이콘/텍스트)를 어둡게 설정 (밝은 배경용)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    // 커스텀 테마 토큰을 CompositionLocalProvider를 통해 제공
    // MaterialTheme.loginTheme, MaterialTheme.authTheme로 접근 가능
    CompositionLocalProvider(
        LocalLoginThemeColors provides LoginThemeColors,
        LocalAuthThemeColors provides AuthThemeColors
    ) {
        // 모든 디자인 토큰을 MaterialTheme에 전달하여 최종 테마 적용
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,    // Type.kt에서 정의된 타이포그래피
            shapes = AppShapes,            // Shape.kt에서 정의된 Shapes (AuthFieldShape, CardShape, DialogShape 포함)
            content = content
        )
    }
}
