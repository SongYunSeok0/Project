@file:Suppress("UnusedImport")

package com.example.scheduler

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val Mint = Color(0xff6ae0d9)
private val MintDark = Color(0xff5db0a8)
private val Yellow = Color(0xfff9c034)
private val GrayText = Color(0xff6a7282)
private val DividerGreen = Color(0xff59c606)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreem(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(1) }
    var selectedDay by remember { mutableIntStateOf(21) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = { Text("복약 리포트", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Mint.copy(alpha = 0.35f)
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .height(24.dp)
                            .width(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xff6ec9dd))
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                tonalElevation = 0.dp,
                containerColor = Color.White
            ) {
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                )
                Spacer(Modifier.weight(1f))
                Column(
                    Modifier.padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(Color(0xff8f8f8f)))
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .size(width = 22.dp, height = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xff9f9f9f))
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Color(0xfffcf8ff))
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = DividerGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DividerGreen,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    selectedContentColor = Color.Black,
                    unselectedContentColor = Color(0xff8f8f8f)
                ) {
                    Text("복약 요약", modifier = Modifier.padding(16.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    selectedContentColor = DividerGreen,
                    unselectedContentColor = Color(0xff8f8f8f)
                ) {
                    Text("개인 트래커", modifier = Modifier.padding(16.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DividerGreen)
                }
            }

            // 주간 헤더
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xffb0b0b0)) }
                Text("Week 4 - May", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = {}) { Icon(Icons.Filled.ArrowForward, null, tint = Color(0xffb0b0b0)) }
            }

            // 요일 라벨
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                    Text(it, color = Color(0xff999999), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 날짜 선택
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (19..25).forEach { day ->
                    val selected = day == selectedDay
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Yellow else Color.Transparent)
                            .clickable { selectedDay = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$day", fontSize = 16.sp, color = if (selected) Color.Black else Color(0xff2b2b2b))
                    }
                }
            }

            // 카드
            Card(
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("5월 21일 (수)", fontSize = 16.sp, lineHeight = 1.5.em, color = Color(0xff101828))
                    Spacer(Modifier.height(16.dp))
                    PillRow(dot = Mint, title = "비타민 D", time = "08:00", trailing = { DoneText() })
                    Spacer(Modifier.height(12.dp))
                    PillRow(dot = Mint, title = "오메가3", time = "12:00", trailing = { DoneText() })
                    Spacer(Modifier.height(12.dp))
                    PillRow(dot = Color(0xffd1d5dc), title = "종합비타민", time = "20:00", trailing = {
                        Text("예정", color = Color(0xff999999), fontSize = 12.sp)
                    })
                }
            }

            // 배너
            Box(
                Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xffe4f5f4))
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("복약 주기를 규칙적으로 지키면", color = MintDark, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 1.43.em)
                    Text("더 정확한 건강 리포트를 받을 수 있어요!", color = MintDark, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 1.43.em)
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PillRow(
    dot: Color,
    title: String,
    time: String,
    trailing: @Composable () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, color = Color(0xff101828), lineHeight = 1.43.em)
                Text(time, fontSize = 12.sp, color = GrayText, lineHeight = 1.33.em)
            }
        }
        trailing()
    }
}

@Composable
private fun DoneText() {
    Text("복용 완료", color = Mint, fontSize = 12.sp)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun SchedulerScreemPreview() {
    SchedulerScreem()
}
