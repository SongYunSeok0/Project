package com.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
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

// ====================================================================
// [1] 공통 박스 래퍼 (BaseCardBox) - 외곽 모양(Shape, Clickable) 정의
// ====================================================================

/**
 * 전체 너비를 차지하는 카드들의 공통적인 외형 속성(클리핑, 배경, 클릭 이벤트)을 정의합니다.
 */
@Composable
private fun BaseCardBox(
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick),
        content = content
    )
}

// ====================================================================
// [2] MainFeatureCard (절반 너비 카드)
// ====================================================================

/**
 * 두 개의 카드가 한 줄에 들어가는 일반적인 사각형 형태의 피처 카드 컴포넌트입니다.
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

// ====================================================================
// [3] FullWidthFeatureCard (통합된 전체 너비 카드 규격)
// ====================================================================

/**
 * 모든 전체 너비 카드의 공통적인 외형 규격(BaseCardBox)을 제공하는 단일 컴포넌트입니다.
 * 내부 콘텐츠와 패딩은 'content' 슬롯을 통해 호출하는 곳에서 정의합니다.
 */
@Composable
fun FullWidthFeatureCard(
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // BaseCardBox를 사용하여 외곽 규격을 통일합니다.
    BaseCardBox(bg = bg, onClick = onClick, modifier = modifier, content = content)
}


/* 1031 18:10 임시주석
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

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

@Composable
fun MainColumnFeatureCard(
    title: String,
    subtitle: String,
    bg: Color,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(icon), title, Modifier.size(48.dp))
        Text(title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

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

*/
