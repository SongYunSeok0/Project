package com.shared.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// Color, Type, Shape를 통합하여 최종 MaterialTheme 컴포저블을 제공하는 메인 테마 파일.

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
    // 오류 팝업
    error = Color.Red,
    onError = Color.White,
    outline = BorderGray
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    // 라이트 테마 ColorScheme 사용
    val colorScheme = LightColorScheme

    // 커스텀 테마 토큰을 CompositionLocalProvider를 통해 제공
    CompositionLocalProvider(
        LocalLoginThemeColors provides LoginThemeColors,
        LocalAuthThemeColors provides AuthThemeColors,
        LocalComponentThemeColors provides ComponentThemeColors
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

@Composable
fun OnlyColorTheme(content: @Composable () -> Unit) {
    // 현재 테마의 폰트 / 모양 유지, 컬러만 맨 위에서 지정한 걸로 변경
    val currentTypography = MaterialTheme.typography

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = currentTypography,
        shapes = AppShapes
    ) {
        content()
    }
}