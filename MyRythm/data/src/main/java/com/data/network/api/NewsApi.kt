package com.data.network.api

import com.data.network.dto.news.NaverNewsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsApi {
    @GET("v1/search/news.json")
    suspend fun getNews(
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim"
    ): NaverNewsResponse
}
