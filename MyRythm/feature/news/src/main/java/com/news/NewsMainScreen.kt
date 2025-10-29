package com.news

import androidx.compose.runtime.Composable
import java.net.URLEncoder

@Composable
fun NewsMainScreen(
    onOpenDetail: (String) -> Unit
) {
    // 내부 NavHost 제거. 리스트만 렌더링하고 클릭 시 상위에 URL 전달
    NewsScreen(
        onOpenDetail = { rawUrl ->
            val encoded = URLEncoder.encode(rawUrl, "UTF-8")
            onOpenDetail(encoded)
        },
        onBack = null
    )
}
