package com.example.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.common.design.R

@Composable
fun MainScreen() {

    val tempIconResId = R.drawable.logo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xfffcfcfc))
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 1) 챗봇 / 스케줄러
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FeatureCard("챗봇", Color(0xffe8f5f4), tempIconResId, Modifier.weight(1f).height(140.dp))
            FeatureCard("스케줄러", Color(0xfff0e8f5), tempIconResId, Modifier.weight(1f).height(140.dp))
        }


        // 2) 걸음수 / 최근 심박수
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FeatureCard("걸음수", Color(0xffe8f0f5), tempIconResId, Modifier.weight(1f).height(140.dp))
            FeatureCard("최근 심박 수", Color(0xffffe8e8), tempIconResId, Modifier.weight(1f).height(140.dp))
        }


        // 3) 복용까지 남은 시간
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xffb5d8f5))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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


        // 4) 지도 / 뉴스
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FeatureCard("지도", Color(0xffe8f5f0), tempIconResId, Modifier.weight(1f).height(140.dp))
            FeatureCard("뉴스", Color(0xfffff4e8), tempIconResId, Modifier.weight(1f).height(140.dp))
        }


        // 5) 건강 인사이트
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xffe8f5f4))
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(tempIconResId), null, Modifier.size(48.dp))
            Text("건강 인사이트", fontSize = 16.sp, color = Color(0xff1e2939))
            Text("오늘의 건강 데이터를 확인하세요", fontSize = 14.sp, color = Color(0xff4a5565), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun FeatureCard(title: String, bg: Color, icon: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(BorderStroke(0.7.dp, Color(0xfff3f4f6)), RoundedCornerShape(14.dp))
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(icon), title, Modifier.size(32.dp))
        Text(title, fontSize = 14.sp, color = Color(0xff1e2939), lineHeight = 1.43.em)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 1000)
@Composable
fun PreviewMain() {
    MainScreen()
}
