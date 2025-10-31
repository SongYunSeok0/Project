package com.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.ui.components.MainFeatureCard
import com.ui.components.FullWidthFeatureCard // 💡 통일된 FullWidthFeatureCard 임포트
import com.ui.theme.AppTypography

// 스크롤 관련 Import
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
    val schedulerIconResId = R.drawable.schedule
    val stepIconResId = R.drawable.step
    val rateIconResId = R.drawable.rate

    val mapIconResId = R.drawable.map
    val newsIconResId = R.drawable.news


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xfffcfcfc))
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .padding(top = 10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 2개씩 정사각형 컴포넌트
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
                icon = schedulerIconResId,
                onClick = onOpenScheduler,
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MainFeatureCard(
                title = "걸음수",
                bg = Color(0x33AD9ABC),
                icon = stepIconResId,
                onClick = onOpenSteps,
                modifier = Modifier.weight(1f).height(140.dp)
            )
            MainFeatureCard(
                title = "최근 심박 수",
                bg = Color(0x33FF7367),
                icon = rateIconResId,
                onClick = onOpenHeart,
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

        // [1] 복용까지 남은 시간 카드 (FullWidthFeatureCard 적용)
        // 외곽 규격은 FullWidthFeatureCard가 처리하고, 내부 레이아웃은 content 슬롯에서 정의합니다.
        FullWidthFeatureCard(
            bg = Color(0x3320FFE5),
            onClick = onOpenScheduler
        ) {
            // 내부 콘텐츠: TimeRemainingCard의 레이아웃 (좌측 정렬, 상하 패딩)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        Modifier.size(48.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("2:30", style = MaterialTheme.typography.displaySmall, color = Color(0xff1e2939))
                Text("10분 전 알림 예정", fontSize = 14.sp, color = Color(0xff4a5565))
            }
        }


        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MainFeatureCard(
                title = "지도",
                bg = Color(0x33C5FF80),
                icon = mapIconResId,
                onClick = onOpenMap,
                modifier = Modifier.weight(1f).height(140.dp),
            )
            MainFeatureCard(
                title = "뉴스",
                bg = Color(0x33FFEF6C),
                icon = newsIconResId,
                onClick = onOpenNews,
                modifier = Modifier.weight(1f).height(140.dp),
            )
        }

        // [2] 건강 인사이트 카드 (FullWidthFeatureCard 적용)
        // 외곽 규격은 FullWidthFeatureCard가 처리하고, 내부 레이아웃은 content 슬롯에서 정의합니다.
        FullWidthFeatureCard(
            bg = Color(0x666AC0E0),
            onClick = { /* 필요 시 다른 목적지로 연결 */ }
        ) {
            // 내부 콘텐츠: MainColumnFeatureCard의 레이아웃 (중앙 정렬, 상하 패딩)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(painterResource(tempIconResId), "건강 인사이트", Modifier.size(48.dp))
                Text("건강 인사이트",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "오늘의 건강 데이터를 확인하세요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme(
        typography = AppTypography
    ) {
        MainScreen()
    }
}


/* 1031 18:10 임시주석
package com.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R
import com.ui.components.MainFeatureCard
import com.ui.components.MainColumnFeatureCard // 💡 MainColumnFeatureCard 임포트
import com.ui.theme.AppTypography

// 스크롤 관련 Import
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
    val schedulerIconResId = R.drawable.schedule
    val stepIconResId = R.drawable.step
    val rateIconResId = R.drawable.rate

    val mapIconResId = R.drawable.map
    val newsIconResId = R.drawable.news


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xfffcfcfc))
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .padding(top = 10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 2개씩 정사각형 컴포넌트
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
                icon = schedulerIconResId,
                onClick = onOpenScheduler,
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MainFeatureCard(
                title = "걸음수",
                bg = Color(0x33AD9ABC),
                icon = stepIconResId,
                onClick = onOpenSteps,
                modifier = Modifier.weight(1f).height(140.dp)
            )
            MainFeatureCard(
                title = "최근 심박 수",
                bg = Color(0x33FF7367),
                icon = rateIconResId,
                onClick = onOpenHeart,
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

        // 복용까지 남은 시간 컴포넌트x
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
                    Modifier.size(48.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("2:30", style = MaterialTheme.typography.displaySmall, color = Color(0xff1e2939))
            Text("10분 전 알림 예정", fontSize = 14.sp, color = Color(0xff4a5565))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MainFeatureCard(
                title = "지도",
                bg = Color(0x33C5FF80),
                icon = mapIconResId,
                onClick = onOpenMap,
                modifier = Modifier.weight(1f).height(140.dp),
            )
            MainFeatureCard(
                title = "뉴스",
                bg = Color(0x33FFEF6C),
                icon = newsIconResId,
                onClick = onOpenNews,
                modifier = Modifier.weight(1f).height(140.dp),
            )
        }
        // 길쭉 컴포넌트
        MainColumnFeatureCard(
            title = "건강 인사이트",
            subtitle = "오늘의 건강 데이터를 확인하세요",
            bg = Color(0x666AC0E0),
            icon = tempIconResId,
            onClick = { */
/* 필요 시 다른 목적지로 연결 *//*
 }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme(
        typography = AppTypography
    ) {
        MainScreen()
    }
}

// 💡 MainColumnFeatureCard를 포함한 카드 컴포넌트 정의는 이제 com/ui/components/MainFeatureCards.kt에 있습니다.


*/
/* 1031 오후 17:50 MainFeatureCard 컴포넌트.kt 별도로 이동
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

 */
