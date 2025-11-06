package com.myrythm.remote.api

import com.myrythm.domain.model.News
import retrofit2.http.GET

interface NewsApi {
    @GET("news")
    suspend fun getNews(): List<News>
}