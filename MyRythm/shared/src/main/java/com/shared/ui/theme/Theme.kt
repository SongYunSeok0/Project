package com.shared.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// Material3 기본 테마
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    surface = Color.White,
    onSurface = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    secondary = Secondary,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceVariant = SurfaceVariant,
    primaryContainer = Color.Transparent,
    secondaryContainer = SecondaryContainer ,
    // 오류 팝업
    error = Color.Red,
    onError = Color.White,
    outline = BorderGray
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    // 항상 라이트 팔레트 사용 (다크 모드와 동일 색)
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        // Activity 캐스팅 대신 SystemUiController 사용
        val systemUiController = rememberSystemUiController()
        SideEffect {
            // 상태바 / 내비바 색을 배경색으로 고정
            systemUiController.setStatusBarColor(
                color = colorScheme.background,
                darkIcons = true   // 밝은 배경 + 어두운 아이콘
            )
            systemUiController.setNavigationBarColor(
                color = colorScheme.background,
                darkIcons = true
            )
        }
    }

    // 커스텀 테마 토큰 제공
    CompositionLocalProvider(
        LocalLoginThemeColors provides LoginThemeColors,
        LocalAuthThemeColors provides AuthThemeColors,
        LocalComponentThemeColors provides ComponentThemeColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

@Composable
fun OnlyColorTheme(content: @Composable () -> Unit) {
    val currentTypography = MaterialTheme.typography

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = currentTypography,
        shapes = AppShapes
    ) {
        content()
    }
}