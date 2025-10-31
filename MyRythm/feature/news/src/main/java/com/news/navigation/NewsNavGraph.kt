package com.news.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.news.ui.NewsDetailScreen
import com.news.ui.NewsMainScreen

fun NavGraphBuilder.newsNavGraph(nav: NavController) {
    composable<NewsRoute> {
        NewsMainScreen(
            nav = nav,
            onOpenDetail = { url -> nav.navigate(NewsDetailRoute(url)) }
        )
    }
    composable<NewsDetailRoute> { e ->
        // ✅ 받을 때 디코딩
        val r = e.toRoute<NewsDetailRoute>()
        val realUrl = Uri.decode(r.url)
        NewsDetailScreen(url = realUrl, onBack = { nav.navigateUp() })
    }
}