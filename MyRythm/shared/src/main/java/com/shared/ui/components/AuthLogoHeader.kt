package com.shared.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shared.R // common 모듈의 R 사용
import com.shared.ui.theme.AuthLogoSize
import com.shared.ui.theme.AuthTextLogoHeight
import com.shared.ui.theme.AuthTextLogoWidth

/**
 * 로그인, 회원가입, 비밀번호 찾기 화면에서 상단 로고를 표시하는 공통 컴포넌트
 *
 * @param textLogoResId 화면별 텍스트 로고 리소스 (ex. R.drawable.login_myrhythm, R.drawable.auth_myrhythm)
 * @param iconSize 아이콘 로고 크기 (기본값은 AuthLogoSize)
 * @param modifier Column 전체 Modifier
 */
@Composable
fun AuthLogoHeader(
    textLogoResId: Int,
    iconSize: Dp = AuthLogoSize,
    modifier: Modifier = Modifier
) {
    val iconLogoText = stringResource(R.string.auth_iconlogo_description)
    val textLogoText = stringResource(R.string.auth_textlogo_description)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = iconLogoText,
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
        )

        Spacer(Modifier.height(16.dp))

        Image(
            painter = painterResource(id = textLogoResId),
            contentDescription = textLogoText,
            modifier = Modifier
                .width(AuthTextLogoWidth)
                .height(AuthTextLogoHeight)
        )

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun AuthLogoIcon(
    iconSize: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val miniIconLogoText = stringResource(R.string.auth_mini_iconlogo_description)
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = miniIconLogoText,
        modifier = Modifier
            .size(iconSize)
            .clip(CircleShape)
    )
}