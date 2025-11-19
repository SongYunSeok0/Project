package com.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.common.design.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean = true,
    onBackClick: () -> Unit = {},
    showSearch: Boolean = false,
    onSearchClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "뒤로가기",
                        tint = Color.Black
                    )
                }
            }
        },
        actions = {
            if (showSearch) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색",
                        tint = Color.Black
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            scrolledContainerColor = Color.White,
            titleContentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
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
            .height(80.dp)
            .background(Color.White)
    ) {

        // 좌/우 탭 버튼 (Home / MyPage)
        Row(
            Modifier
                .fillMaxSize()
                .shadow(2.dp)
                .padding(horizontal = 50.dp)
                .zIndex(1f),
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

        // 중앙 알약 버튼
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-25).dp)
                .shadow(8.dp, CircleShape, clip = false)
                .size(75.dp)
                .clip(CircleShape)
                .background(Color(0xFF6AE0D9))
                .zIndex(2f)
                .clickable {
                    onTabSelected("Schedule")  // ⭐ 핵심
                },
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