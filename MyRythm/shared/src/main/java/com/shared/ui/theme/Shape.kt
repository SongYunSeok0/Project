package com.shared.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// 버튼, 카드 등 컴포넌트의 모서리 모양 정의

val SmallButthonInputFieldShape = RoundedCornerShape(8.dp)
val ButthonInputFieldShape = RoundedCornerShape(12.dp)
val CardShape = RoundedCornerShape(13.dp)
val DialogShape = RoundedCornerShape(16.dp)

// Material3 Shapes 객체
// (Theme.kt에서 사용)
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = SmallButthonInputFieldShape,    // 8  작은버튼/입력필드용
    medium = ButthonInputFieldShape,        // 12.dp - 입력 필드, 버튼용
    large = CardShape,              // 13.dp - 카드용
    extraLarge = DialogShape        // 16.dp - 다이얼로그용
)