package com.news.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.news.ui.NewsDetailScreen
import com.news.ui.NewsMainScreen

fun NavGraphBuilder.newsNavGraph(nav: NavController, userId: String) {
    composable<NewsRoute> { backStackEntry ->

        // 🔥 SavedStateHandle에 userId 저장
        backStackEntry.savedStateHandle["userId"] = userId

        // 🔥 ViewModel은 자동으로 savedStateHandle을 받아감
        NewsMainScreen(
            nav = nav,
            onOpenDetail = { url -> nav.navigate(NewsDetailRoute(url)) }
        )
    }

    composable<NewsDetailRoute> { entry ->
        val r = entry.toRoute<NewsDetailRoute>()
        val realUrl = Uri.decode(r.url)
        NewsDetailScreen(url = realUrl, onBack = { nav.navigateUp() })
    }
}
