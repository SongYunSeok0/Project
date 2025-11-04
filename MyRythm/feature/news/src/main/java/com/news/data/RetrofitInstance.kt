package com.news.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//data 모듈의 remote/di로 옮기거나 Hilt써야함
object RetrofitInstance {
    private const val BASE_URL = "https://openapi.naver.com/"

    val api: NaverNewsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverNewsApi::class.java)
    }
}
