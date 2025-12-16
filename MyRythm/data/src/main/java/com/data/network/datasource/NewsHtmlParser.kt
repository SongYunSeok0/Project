package com.data.network.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NewsHtmlParser {

    suspend fun fetchThumbnail(url: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(3000)
                    .get()

                doc.select("meta[property=og:image]")
                    .attr("content")
                    .takeIf { it.isNotEmpty() }

            } catch (e: Exception) {
                null
            }
        }
}
