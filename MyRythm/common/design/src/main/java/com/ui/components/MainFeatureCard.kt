package com.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/*
    MainScreen.kt
    메인스크린 홈의 featureCard 컴포넌트화
    공통: 팝업 카드의 규격+간격
    차이: 팝업 카드의 컬러, 텍스트 내용, 아이콘 이미지

    1. row 2개 형태
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FeatureCard(
                title = "챗봇",
                bg = Color(0xffe8f5f4),
                icon = tempIconResId,
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = onOpenChatBot
            )
            FeatureCard(
                title = "스케줄러",
                bg = Color(0xfff0e8f5),
                icon = tempIconResId,
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = onOpenScheduler
            )
        }

     2. Column + row 1개 형태
     Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xffb5d8f5))
                .clickable { onOpenScheduler() } // 남은시간 카드 → 스케줄러로 이동
                .padding(20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("복용까지 남은 시간", fontSize = 16.sp, color = Color(0xff1e2939))
                Image(painterResource(tempIconResId), null, Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("2:30", style = MaterialTheme.typography.displaySmall, color = Color(0xff1e2939))
            Text("10분 전 알림 예정", fontSize = 14.sp, color = Color(0xff4a5565))
        }
 */


@Composable
fun MainFeatureCard(
    title: String,
    bg: Color,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        Image(painterResource(icon), title, Modifier.size(40.dp))
        Text(title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

