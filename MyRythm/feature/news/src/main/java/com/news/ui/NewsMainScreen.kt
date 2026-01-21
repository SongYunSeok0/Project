package com.news.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.news.NewsViewModel

@Composable
fun NewsMainScreen(
    nav: NavController,
    onOpenDetail: (String) -> Unit
) {
    val viewModel: NewsViewModel = hiltViewModel()

    // 🔥 AppRoot에서 넘어온 이벤트 받기
    val openSearch = nav.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("openSearch", false)
        ?.collectAsState()

    // 이벤트 감지하면 검색 모드 켜기
    LaunchedEffect(openSearch?.value) {
        if (openSearch?.value == true) {
            viewModel.openSearch()
            // 이벤트 초기화
            nav.currentBackStackEntry?.savedStateHandle?.set("openSearch", false)
        }
    }

    // 기존 화면 구성
    NewsScreen(
        nav = nav,
        onOpenDetail = onOpenDetail,
        viewModel = viewModel
    )
}
