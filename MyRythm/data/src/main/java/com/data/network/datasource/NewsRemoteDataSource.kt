package com.data.network.datasource

import com.data.network.api.NewsApi

class NewsRemoteDataSource(
    private val api: NewsApi
) {
    suspend fun fetchNews(
        query: String,
        display: Int,
        start: Int
    ) = api.getNews(
        query = query,
        display = display,
        start = start,
        sort = "date"
    )
}
