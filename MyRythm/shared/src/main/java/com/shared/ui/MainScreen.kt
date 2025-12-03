package com.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.bar.AppBottomBar
import com.shared.ui.components.FullWidthFeatureCard
import com.shared.ui.components.MainFeatureCard
import com.shared.ui.theme.AppTypography
import com.shared.ui.theme.componentTheme

@Composable
fun MainScreen(
    onOpenChatBot: () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenAlram: () -> Unit = {},
    onOpenHeart: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    nextTime: String? = null,
    todaySteps: Int = 0,
    remainText: String? = null,
    nextLabel: String? = null,
) {
    val tempIconResId = R.drawable.logo
    val chatBotIconResId = R.drawable.robot
    val schedulerIconResId = R.drawable.schedule
    val stepIconResId = R.drawable.step
    val rateIconResId = R.drawable.rate
    val mapIconResId = R.drawable.map
    val newsIconResId = R.drawable.news

    val chatbotText = stringResource(R.string.chatbot)
    val schedulerText = stringResource(R.string.scheduler)
    val rateText = stringResource(R.string.rate)
    val timeremainder = stringResource(R.string.timeremainder)
    val mapText = stringResource(R.string.map)
    val newsText = stringResource(R.string.news)
    val healthinsightText = stringResource(R.string.healthinsight)
    val healthinsightMessage = stringResource(R.string.main_message_healthinsight)


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .padding(bottom = 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Spacer(modifier = Modifier.height(1.dp))

        FullWidthFeatureCard(
            bg = MaterialTheme.componentTheme.healthInsightCard,
            onClick = { /* 연결 필요하면 추가 */ }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painterResource(tempIconResId),
                    healthinsightText,
                    Modifier.size(48.dp)
                )
                Text(
                    healthinsightText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    healthinsightMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MainFeatureCard(
                title = "${todaySteps}걸음",
                bg = MaterialTheme.componentTheme.stepCard,
                icon = stepIconResId,
                modifier = Modifier.weight(1f).height(140.dp),
            )

            MainFeatureCard(
                title = schedulerText,
                bg = MaterialTheme.componentTheme.schedulerCard,
                icon = schedulerIconResId,
                onClick = onOpenScheduler,
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

        // 복용 알림 카드 (클릭 연결)
        FullWidthFeatureCard(
            bg = MaterialTheme.componentTheme.timeRemainingCard,
            onClick = onOpenAlram
        ) {
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
                    Text(
                        timeremainder,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Image(
                        painterResource(tempIconResId),
                        null,
                        Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ⭐ 여기를 "좌(label) - 우(시간)" 로 나누기
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp), // 카드 좌우에서 살짝 안쪽으로
                    horizontalArrangement = Arrangement.SpaceEvenly, // 중앙 배치
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Label
                    Text(
                        text = nextLabel ?: "다음 복용",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 8.dp) // 너무 붙지 않게 약간 간격
                    )

                    // Time
                    Text(
                        text = remainText ?: "--:--",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MainFeatureCard(
                title = chatbotText,
                bg = MaterialTheme.componentTheme.chatbotCard,
                icon = chatBotIconResId,
                onClick = onOpenChatBot,
                modifier = Modifier.weight(1f).height(140.dp)
            )
            MainFeatureCard(
                title = rateText,
                bg = MaterialTheme.componentTheme.rateCard,
                icon = rateIconResId,
                onClick = onOpenHeart,
                modifier = Modifier.weight(1f).height(140.dp),
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MainFeatureCard(
                title = mapText,
                bg = MaterialTheme.componentTheme.mapCard,
                icon = mapIconResId,
                onClick = onOpenMap,
                modifier = Modifier.weight(1f).height(140.dp)
            )
            MainFeatureCard(
                title = newsText,
                bg = MaterialTheme.componentTheme.newsCard,
                icon = newsIconResId,
                onClick = onOpenNews,
                modifier = Modifier.weight(1f).height(140.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun MainScreenRealPreview() {
    MaterialTheme(typography = AppTypography) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 콘텐츠 영역
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)  // AppRoot padding
            ) {
                MainScreen()
            }

            // 바텀바 (하단 고정)
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                AppBottomBar(
                    currentScreen = "Home",
                    onTabSelected = {}
                )
            }
        }
    }
}