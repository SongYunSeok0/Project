package com.map.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class NaverSearchResponse(
    val items: List<PlaceItem>
)

interface NaverSearchApi {
    @GET("v1/search/local.json")
    suspend fun searchPlaces(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "random",
        @Query("coordinate") coordinate: String? = null
    ): NaverSearchResponse
}
