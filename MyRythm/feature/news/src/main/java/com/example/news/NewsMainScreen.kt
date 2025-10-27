package com.example.news

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.net.URLDecoder

@Composable
fun NewsMainScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "news_list"
    ) {
        composable("news_list") {
            NewsScreen(navController = navController)
        }

        composable("news_detail/{url}") { backStackEntry ->
            val url = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8")
            NewsDetailScreen(url = url, navController = navController)
        }
    }
}
