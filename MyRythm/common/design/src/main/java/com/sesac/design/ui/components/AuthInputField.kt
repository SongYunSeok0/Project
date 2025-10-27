package com.sesac.design.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sesac.design.ui.theme.*

/**
 * 이름, 이메일, 비밀번호 등 인증 화면의 공통 입력 필드 스타일을 정의합니다.
 * (실제 입력 기능은 포함하지 않고 Placeholder 스타일만 재현합니다.)
 *
 * @param hint Placeholder 텍스트
 */

@Composable
fun AuthInputField(
    hint: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            shape = AuthFieldShape,
            color = AuthPrimaryButton,
            modifier = Modifier
                .requiredWidth(AuthFieldWidth)
                .requiredHeight(AuthFieldHeight)
                .shadow(elevation = ShadowElevationDefault, shape = AuthFieldShape)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                // 실제 TextField 대신 Placeholder 텍스트만 표시
                Text(
                    text = hint,
                    color = AuthOnPrimary,
                    fontSize = 15.sp,
                    letterSpacing = 0.9.sp
                )
            }
        }
    }
}
