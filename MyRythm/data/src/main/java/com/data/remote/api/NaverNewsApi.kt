package com.data.remote.api
import com.data.remote.dto.NaverNewsResponse
import retrofit2.http.GET
import retrofit2.http.Query


//data모듈의 remote/api로 옮겨야함
interface NaverNewsApi {
    @GET("v1/search/news.json")
    suspend fun getNews(
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim"
    ): NaverNewsResponse
}