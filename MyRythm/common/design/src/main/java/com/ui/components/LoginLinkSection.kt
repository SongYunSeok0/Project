package com.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ui.theme.AuthLinkButtonShape
import com.ui.theme.AuthLoginSecondrayButton
import com.ui.theme.PointText
import com.ui.theme.PrimaryText
import com.ui.theme.ShadowElevationLink

/**
 * "이미 계정이 있으신가요? 로그인" 형태의 하단 링크 섹션
 * @param onLoginClick '로그인' 버튼 클릭 시 실행될 람다 함수
 */
@Composable
fun LoginLinkSection(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().height(28.dp)
    ) {
        // 안내 텍스트
        Text(
            text = "이미 계정이 있으신가요?",
            color = PrimaryText,
            fontSize = 14.sp,
            letterSpacing = 0.84.sp
        )
        Spacer(modifier = Modifier.width(8.dp)) // 텍스트와 버튼 사이 간격

        // '로그인' 버튼 (서브 버튼 스타일)
        Surface(
            onClick = onLoginClick,
            shape = AuthLinkButtonShape,
            color = AuthLoginSecondrayButton,
            modifier = Modifier
                .height(28.dp)
                .clip(shape = AuthLinkButtonShape)
                .shadow(elevation = ShadowElevationLink, shape = AuthLinkButtonShape)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "로그인",
                    color = PointText,
                    fontSize = 14.sp,
                    letterSpacing = 0.84.sp
                )
            }
        }
    }
}
