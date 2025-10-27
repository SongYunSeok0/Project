package com.example.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.common.design.R
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBackClick: () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, fontSize = 18.sp, color = Color.Black) },
        navigationIcon = {
            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .clickable { onBackClick() }
            )
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
        // 하단 네비게이션 아이콘들
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 홈 아이콘
            IconButton(onClick = { onTabSelected("Home") }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "홈",
                    tint = if (currentScreen == "Home") Color(0xff6ae0d9) else Color.Gray
                )
            }

            // 마이페이지 아이콘
            IconButton(onClick = { onTabSelected("MyPage") }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "마이",
                    tint = if (currentScreen == "MyPage") Color(0xff6ae0d9) else Color.Gray
                )
            }
        }

        // 가운데 둥근 돌출 버튼
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-25).dp) // 더 위로 띄워서 돌출 효과
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false
                )
                .size(75.dp)
                .clip(CircleShape)
                .background(Color(0xff6ae0d9)) // 민트색 버튼
                .clickable { onTabSelected("Schedule") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pill), // ✅ 우리가 정한 이미지
                contentDescription = "스케줄 버튼",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

