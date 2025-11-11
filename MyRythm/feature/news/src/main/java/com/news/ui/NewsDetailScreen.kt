package com.news.ui

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NewsDetailScreen(
    url: String,
    onBack: () -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // ✅ 일부 Android 버전에서는 SafeBrowsing이 HTTP 링크를 차단하므로 해제
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    settings.safeBrowsingEnabled = false
                }

                // ✅ 커스텀 WebViewClient 설정 (내부에서 링크 계속 열리게)
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        request?.url?.let {
                            view?.loadUrl(it.toString())
                        }
                        return true
                    }
                }

                // ✅ HTTP → HTTPS 변환 시도 (https 지원 사이트일 경우 자동 전환)
                val fixedUrl = if (url.startsWith("http://")) {
                    url.replaceFirst("http://", "https://")
                } else url

                loadUrl(fixedUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
