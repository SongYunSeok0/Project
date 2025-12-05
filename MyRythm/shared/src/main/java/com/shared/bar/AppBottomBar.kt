package com.shared.bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.shared.R

@Composable
fun AppBottomBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    val barHeight = 80.dp                   // 바텀바 기본 높이
    val floatingSize = 80.dp                // 플로팅 버튼 크기
    val floatingOffset = -(floatingSize / 2) // 플로팅 오프셋 = 자동 반응형

    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(Color(0xFFF7FDFC))
    ) {

        // 좌/우 탭 버튼 (Home / MyPage)
        Row(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 50.dp)
                .zIndex(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onTabSelected("Home") }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "홈",
                    tint = if (currentScreen == "Home") Color(0xFF6AE0D9) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = { onTabSelected("MyPage") }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "마이",
                    tint = if (currentScreen == "MyPage") Color(0xFF6AE0D9) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // 중앙 알약 버튼
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = floatingOffset)
                .shadow(8.dp, CircleShape, clip = false)
                .size(floatingSize)
                .clip(CircleShape)
                .background(Color(0xFF6AE0D9))
                .zIndex(2f)
                .clickable {
                    onTabSelected("Schedule")  // ⭐ 핵심
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pill),
                contentDescription = "스케줄",
                modifier = Modifier.size(floatingSize * 0.5f)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun AppBottomBarPreview() {

    // 프리뷰 용으로 위쪽 공간을 확보한 Wrapper 박스
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)   // 👈 프리뷰 영역 크게 확보
            .background(Color(0xFFF5F5F5))
    ) {

        // 바텀바는 하단에 붙여서 표시
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            AppBottomBar(
                currentScreen = "Home",
                onTabSelected = {}
            )
        }
    }
}

