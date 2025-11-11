package com.data.network.api

import com.domain.model.News
import retrofit2.http.GET

interface NewsApi {
    @GET("news")
    suspend fun getNews(): List<News>
}