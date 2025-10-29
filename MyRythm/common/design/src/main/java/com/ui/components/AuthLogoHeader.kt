package com.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.common.design.R // common 모듈의 R 사용
import com.ui.theme.AuthLogoSize
import com.ui.theme.AuthTextLogoHeight
import com.ui.theme.AuthTextLogoWidth

/**
 * 회원가입 및 비밀번호 찾기 화면의 상단 로고와 텍스트 로고를 표시하는 컴포넌트
 * @param textLogoResId R.drawable.auth_myrhythm 또는 R.drawable.login_myrhythm
 */
@Composable
fun AuthLogoHeader(
    textLogoResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 80.dp) // SignupScreen 원본 코드의 .offset(y = 80.dp) 효과
    ) {
        // 공통 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "MyRhythm Logo Icon",
            modifier = Modifier
                .requiredSize(size = AuthLogoSize)
                .clip(CircleShape)
        )
        // 화면별 텍스트 로고 이미지
        Image(
            painter = painterResource(id = textLogoResId),
            contentDescription = "My Rhythm Text Logo",
            modifier = Modifier
                .padding(top = 16.dp)
                .requiredWidth(AuthTextLogoWidth)
                .requiredHeight(AuthTextLogoHeight)
        )
    }
}
