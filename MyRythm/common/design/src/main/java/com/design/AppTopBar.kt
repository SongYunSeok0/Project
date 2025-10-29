package com.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.common.design.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean = true,
    onBackClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontSize = 18.sp, color = Color.Black) },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "뒤로가기",
                        tint = Color.Black
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    )
}

@Composable
fun AppBottomBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(Color.White)
    ) {
        // 좌·우 탭
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp)
                .zIndex(1f), // 클릭 보장
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onTabSelected("Home") }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "홈",
                    tint = if (currentScreen == "Home") Color(0xFF6AE0D9) else Color.Gray
                )
            }
            IconButton(onClick = { onTabSelected("MyPage") }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "마이",
                    tint = if (currentScreen == "MyPage") Color(0xFF6AE0D9) else Color.Gray
                )
            }
        }

        // 중앙 알약
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-25).dp)
                .shadow(8.dp, CircleShape, clip = false)
                .size(75.dp)
                .clip(CircleShape)
                .background(Color(0xFF6AE0D9))
                .zIndex(2f) // 최상단
                .clickable { onTabSelected("Schedule") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pill),
                contentDescription = "스케줄",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
