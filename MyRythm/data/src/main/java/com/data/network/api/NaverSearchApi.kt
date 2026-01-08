package com.data.network.api

import com.data.network.dto.map.SearchResult
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverSearchApi {
    @GET("v1/search/local.json")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("display") display: Int = 50,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim"
    ): SearchResult
}