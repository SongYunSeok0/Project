package com.news

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.net.URLDecoder

@Composable
fun NewsMainScreen(onBack: () -> Unit = {}) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "news_list") {
        composable("news_list") {
            // 항상 상위 콜백 전달 → 목록에서도 뒤로가기 아이콘 표시
            NewsScreen(
                navController = navController,
                onBack = onBack
            )
        }
        composable("news_detail/{url}") { backStackEntry ->
            val url = URLDecoder.decode(
                backStackEntry.arguments?.getString("url") ?: "", "UTF-8"
            )
            NewsDetailScreen(url = url, navController = navController)
        }
    }
}