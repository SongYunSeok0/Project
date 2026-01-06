package com.news.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.news.viewmodel.NewsViewModel

@Composable
fun NewsMainScreen(
    nav: NavController,
    onOpenDetail: (String) -> Unit
) {
    val newsViewModel: NewsViewModel = hiltViewModel()

    val openSearch = nav.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("openSearch", false)
        ?.collectAsState()

    LaunchedEffect(openSearch?.value) {
        if (openSearch?.value == true) {
            newsViewModel.openSearch()
            nav.currentBackStackEntry
                ?.savedStateHandle
                ?.set("openSearch", false)
        }
    }

    NewsScreen(
        nav = nav,
        onOpenDetail = onOpenDetail,
        newsViewModel = newsViewModel
        // FavoriteViewModel은 NewsScreen 내부에서 hiltViewModel()
    )
}

