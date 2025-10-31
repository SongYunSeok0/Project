package com.main.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.ui.components.MainFeatureCard
import com.ui.theme.AppTypography

@Composable
fun MainScreen(
    onOpenChatBot:   () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenSteps:     () -> Unit = {},
    onOpenHeart:     () -> Unit = {},
    onOpenMap:       () -> Unit = {},
    onOpenNews:      () -> Unit = {},
    onFabCamera:     () -> Unit = {} // 현재 화면에서는 미사용
) {
    val tempIconResId = R.drawable.logo
    val chatBotIconResId = R.drawable.robot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xfffcfcfc))
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MainFeatureCard(
                title = "챗봇",
                bg = Color(0x3380E1FF),
                icon = chatBotIconResId,
                onClick = onOpenChatBot,
                modifier = Modifier.weight(1f).height(140.dp)
            )
            MainFeatureCard(
                title = "스케줄러",
                bg = Color(0x33EB80FF),
                icon = tempIconResId,
                onClick = onOpenScheduler,
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MainFeatureCard(
                title = "걸음수",
                bg = Color(0x33AD9ABC),
                icon = tempIconResId,
                onClick = onOpenSteps,
                modifier = Modifier.weight(1f).height(140.dp)
            )
            MainFeatureCard(
                title = "최근 심박 수",
                bg = Color(0x33FF7367),
                icon = tempIconResId,
                onClick = onOpenHeart,
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x3320FFE5))
                .clickable { onOpenScheduler() } // 남은시간 카드 → 스케줄러로 이동
                .padding(20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("복용까지 남은 시간", fontSize = 16.sp, color = Color(0xff1e2939))
                Image(painterResource(tempIconResId),
                    null,
                    Modifier.size(32.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("2:30", style = MaterialTheme.typography.displaySmall, color = Color(0xff1e2939))
            Text("10분 전 알림 예정", fontSize = 14.sp, color = Color(0xff4a5565))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FeatureCard(
                title = "지도",
                bg = Color(0x33C5FF80),
                icon = tempIconResId,
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = onOpenMap
            )
            FeatureCard(
                title = "뉴스",
                bg = Color(0x33FFEF6C),
                icon = tempIconResId,
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = onOpenNews
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x666AC0E0))
                .clickable { /* 필요 시 다른 목적지로 연결 */ }
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(tempIconResId), null, Modifier.size(48.dp))
            Text("건강 인사이트", fontSize = 16.sp, color = Color(0xff1e2939))
            Text(
                "오늘의 건강 데이터를 확인하세요",
                fontSize = 14.sp, color = Color(0xff4a5565), textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    bg: Color,
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(BorderStroke(0.7.dp, Color(0xfff3f4f6)), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(icon), title, Modifier.size(32.dp))
        Text(title, fontSize = 14.sp, color = Color(0xff1e2939), lineHeight = 1.43.em)
    }
}

@Preview(showBackground = true, name = "메인 화면 미리보기")
@Composable
fun MainScreenPreview() {
    // 미리보기 환경에 필요한 MaterialTheme과 AppTypography를 적용합니다.
    MaterialTheme(
        typography = AppTypography
    ) {
        // 원본 MainScreen 컴포저블을 렌더링합니다.
        MainScreen()
    }
}