package com.shared.bar

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.shared.R
import com.shared.ui.theme.AppTheme
/*
@Composable
fun AppBottomBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    val barHeight = 80.dp                   // 바텀바 기본 높이
    val floatingSize = 80.dp                // 플로팅 버튼 크기
    val floatingOffset = -(floatingSize * 0.25f) // 플로팅 오프셋 = 자동 반응형

    val homeText = stringResource(R.string.home)
    val mypageText = stringResource(R.string.mypage)
    val scheduleText = stringResource(R.string.schedule)

    AppTheme {
        Box(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(barHeight)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 50.dp)
                    .zIndex(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onTabSelected("Home") }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = homeText,
                        tint = if (currentScreen == "Home") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { onTabSelected("MyPage") }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = mypageText,
                        tint = if (currentScreen == "MyPage") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
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
                    .background(MaterialTheme.colorScheme.primary)
                    .zIndex(2f)
                    .clickable {
                        onTabSelected("Schedule")
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pill),
                    contentDescription = scheduleText,
                    modifier = Modifier.size(floatingSize * 0.5f)
                )
            }
        }
    }
}


// 1206 바텀바 반응형 적용중
@Composable
fun ResponsiveAppBottomBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    val navBarPadding = WindowInsets
        .navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Box(modifier = Modifier.fillMaxWidth()) {
        // 네 원본 바텀바 그대로
        AppBottomBar(
            currentScreen = currentScreen,
            onTabSelected = onTabSelected
        )
    }

    // ⭐ navigation bar가 있을 때만 자동으로 공간 생김
    // ⭐ 없으면 0dp라서 일반 기기에서는 깔끔하게 딱 붙음
    Spacer(modifier = Modifier.height(navBarPadding))
}

//1206
@Composable
fun ResponsiveAppBottomBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    val systemUi = rememberSystemUiController()
    val density = LocalDensity.current

    // 제스처 여부 판단
    val bottomInset = WindowInsets.navigationBars.getBottom(density)
    val barColor = MaterialTheme.colorScheme.secondaryContainer

    LaunchedEffect(bottomInset) {
        if (bottomInset == 0) {
            // ⭐ 제스처 모드 → 바텀바 색과 동일하게
            systemUi.setNavigationBarColor(
                color = barColor,
                darkIcons = false
            )
        } else {
            // ⭐ 3버튼 내비바 → 원래 시스템 기본 유지
            systemUi.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = true
            )
        }
    }

    AppBottomBar(
        currentScreen = currentScreen,
        onTabSelected = onTabSelected
    )
}
*/
@Composable
fun AppBottomBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit,
    addPadding: Boolean = true,
    isGestureMode: Boolean = false // ⭐ 제스처 모드 플래그 추가
) {
    val barHeight = 80.dp
    val floatingSize = 80.dp
    val floatingOffset = -(floatingSize * 0.25f)
    val homeText = stringResource(R.string.home)
    val mypageText = stringResource(R.string.mypage)
    val scheduleText = stringResource(R.string.schedule)

    // ⭐ 제스처 모드일 때 네비바 높이 계산
    val density = LocalDensity.current
    val navBarHeight = if (isGestureMode) {
        with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    } else 0.dp

    AppTheme {
        Box(
            Modifier
                .fillMaxWidth()
                .then(
                    if (addPadding) Modifier.navigationBarsPadding()
                    else Modifier
                )
                .height(barHeight + navBarHeight) // ⭐ 제스처 모드일 때 높이 확장
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            // ⭐ 실제 콘텐츠는 barHeight만큼만 차지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 50.dp)
                        .zIndex(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onTabSelected("Home") }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = homeText,
                            tint = if (currentScreen == "Home") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = { onTabSelected("MyPage") }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = mypageText,
                            tint = if (currentScreen == "MyPage") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = floatingOffset)
                        .shadow(8.dp, CircleShape, clip = false)
                        .size(floatingSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .zIndex(2f)
                        .clickable { onTabSelected("Schedule") },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pill),
                        contentDescription = scheduleText,
                        modifier = Modifier.size(floatingSize * 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ResponsiveAppBottomBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    val systemUi = rememberSystemUiController()
    val density = LocalDensity.current
    val bottomInset = WindowInsets.navigationBars.getBottom(density)
    val barColor = MaterialTheme.colorScheme.secondaryContainer

    val isGestureMode = bottomInset == 0

    LaunchedEffect(bottomInset) {
        Log.d("BottomBar", "🔍 bottomInset: $bottomInset, isGestureMode: $isGestureMode")
    }

    LaunchedEffect(bottomInset, barColor) {
        if (isGestureMode) {
            // 제스처 모드: 네비바 투명
            systemUi.setNavigationBarColor(
                color = barColor,
                darkIcons = false,
                navigationBarContrastEnforced = false
            )
        } else {
            // 3버튼 모드: 네비바를 바텀바와 같은 색으로
            systemUi.setNavigationBarColor(
                color = barColor,
                darkIcons = true
            )
        }
    }

    AppBottomBar(
        currentScreen = currentScreen,
        onTabSelected = onTabSelected,
        addPadding = !isGestureMode, // 제스처 모드가 아닐 때만 패딩
        isGestureMode = isGestureMode // ⭐ 제스처 모드 플래그 전달
    )
}