package com.news

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import java.net.URLEncoder

@Composable
fun NewsMainScreen(
    nav: NavController,
    onOpenDetail: (String) -> Unit
) {
    NewsScreen(
        nav = nav,
        onOpenDetail = { rawUrl ->
            val encoded = URLEncoder.encode(rawUrl, "UTF-8")
            onOpenDetail(encoded)
        }
    )
}
