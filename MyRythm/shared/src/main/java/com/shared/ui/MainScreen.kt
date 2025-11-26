package com.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.shared.ui.components.FullWidthFeatureCard
import com.shared.ui.components.MainFeatureCard
import com.shared.ui.theme.AppTypography
import com.shared.ui.theme.componentTheme

@Composable
fun MainScreen(
    onOpenChatBot: () -> Unit = {},
    onOpenScheduler: () -> Unit = {},
    onOpenAlram: () -> Unit = {}, // ✅ 알람 카드 클릭 시 호출될 콜백
    onOpenHeart: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    nextTime: String? = null,
    todaySteps: Int = 0,
    remainText: String? = null,
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
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .padding(top = 10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

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
                title = schedulerText,
                bg = MaterialTheme.componentTheme.schedulerCard,
                icon = schedulerIconResId,
                onClick = onOpenScheduler,
                modifier = Modifier.weight(1f).height(140.dp)
            )
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
                title = rateText,
                bg = MaterialTheme.componentTheme.rateCard,
                icon = rateIconResId,
                onClick = onOpenHeart,
                modifier = Modifier.weight(1f).height(140.dp),
            )
        }

        // ⭐ 복용 알림 카드 (클릭 연결)
        FullWidthFeatureCard(
            bg = MaterialTheme.componentTheme.timeRemainingCard,
            onClick = onOpenAlram // ✅ Route에서 전달받은 TimePicker 열기 함수 연결
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

                // 남은 시간 표시
                Text(
                    remainText ?: "--:--",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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