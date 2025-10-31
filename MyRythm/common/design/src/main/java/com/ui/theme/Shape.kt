package com.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// 버튼, 카드 등 컴포넌트의 모서리 모양 정의

val AuthFieldShape = RoundedCornerShape(10.dp)
val CardShape = RoundedCornerShape(12.dp)
val DialogShape = RoundedCornerShape(16.dp)

// Material3 Shapes 객체 (Theme.kt에서 사용)
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = AuthFieldShape,        // 10.dp - 입력 필드, 버튼용
    large = CardShape,              // 12.dp - 카드용
    extraLarge = DialogShape        // 16.dp - 다이얼로그용
)