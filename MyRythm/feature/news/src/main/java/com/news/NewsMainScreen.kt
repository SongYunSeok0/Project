package com.news

import androidx.compose.runtime.Composable
import java.net.URLEncoder

@Composable
fun NewsMainScreen(
    onOpenDetail: (String) -> Unit
) {
    NewsScreen(
        onOpenDetail = { rawUrl ->
            val encoded = URLEncoder.encode(rawUrl, "UTF-8")
            onOpenDetail(encoded)
        },
        onBack = null
    )
}
