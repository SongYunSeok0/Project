package com.news

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.navigation.*

fun NavGraphBuilder.newsNavGraph(nav: NavController) {
    composable<NewsRoute> {
        NewsMainScreen(
            onOpenDetail = { url -> nav.navigate(NewsDetailRoute(url)) }
        )
    }
    composable<NewsDetailRoute> { e ->
        val r = e.toRoute<NewsDetailRoute>()
        NewsDetailScreen(url = r.url, onBack = { nav.navigateUp() })
    }
}
