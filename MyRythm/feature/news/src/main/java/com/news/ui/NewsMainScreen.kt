package com.news.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun NewsMainScreen(
    nav: NavController,
    onOpenDetail: (String) -> Unit
) {
    // NewsScreen은 단순히 onOpenDetail을 위로 전달만 하면 된다.
    NewsScreen(
        nav = nav,
        onOpenDetail = onOpenDetail
    )
}
