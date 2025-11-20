package com.shared.ui.theme
import com.shared.R
import androidx.compose.material3.Typography

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 앱의 모든 텍스트 스타일 정의. (Typography 객체, FontFamily 정의).

// 기본 폰트
val defaultFontFamily = FontFamily.Default
val pretendard = FontFamily(
    Font(R.font.pretendardvariable, FontWeight.Normal)
)
val AppTypography = Typography(
    // 제목 글씨
    headlineMedium = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp, // 크고 굵게 (약 24sp)
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // 카드 제목, 섹션 제목 등
    titleMedium = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // 주요 버튼 글씨
    labelLarge = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // 입력 필드와 본문 글씨
    bodyLarge = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // 안내메시지 등 작은 글씨
    bodySmall = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
)



    /*
    val AppTypography = Typography( // 👈 '큰 바구니' (MaterialTheme에 전달할 전체 꾸러미)
        displayLarge = TextStyle( // 👈 '개별 스타일' (이 스타일의 모든 속성 묶음)
            // 이 안에 폰트 크기, 굵기 등 '개별 속성 재료'가 들어갑니다.
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp, // 👈 '개별 속성 재료'
            lineHeight = 44.sp,
            // ...
        ),
// ... 나머지 19개 스타일 ...
)
     */