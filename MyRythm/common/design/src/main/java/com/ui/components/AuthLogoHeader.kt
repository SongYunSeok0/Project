package com.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.common.design.R // common 모듈의 R 사용
import com.ui.theme.AuthLogoSize
import com.ui.theme.AuthTextLogoHeight
import com.ui.theme.AuthTextLogoWidth

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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // 1️⃣ 아이콘 로고
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "MyRhythm Logo Icon",
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
        )

        Spacer(Modifier.height(16.dp)) // 아이콘과 텍스트 로고 간 간격

        // 2️⃣ 텍스트 로고
        Image(
            painter = painterResource(id = textLogoResId),
            contentDescription = "My Rhythm Text Logo",
            modifier = Modifier
                .width(AuthTextLogoWidth)
                .height(AuthTextLogoHeight)
        )

        Spacer(Modifier.height(30.dp)) // 텍스트 로고와 입력 필드 간 간격
    }
}

@Composable
fun AuthLogoIcon(
    iconSize: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Mini logo Icon",
        modifier = Modifier
            .size(iconSize)
            .clip(CircleShape)
    )
}


/*
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

*/
/**
 * 회원가입 및 비밀번호 찾기 화면의 상단 로고와 텍스트 로고를 표시하는 컴포넌트
 * @param textLogoResId R.drawable.auth_myrhythm 또는 R.drawable.login_myrhythm
 *//*

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
*/
