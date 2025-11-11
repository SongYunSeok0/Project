package com.news.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

//data모듈의 remote/api로 옮겨야함
interface NaverNewsApi {
    @GET("v1/search/news.json")
    suspend fun getNews(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim"
    ): NaverNewsResponse
}
