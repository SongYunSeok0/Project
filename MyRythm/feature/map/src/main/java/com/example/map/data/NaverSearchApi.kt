package com.example.map.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// API 응답 구조에 맞는 데이터 클래스
data class SearchResult(
    val items: List<PlaceItem>
)

// 장소 정보를 담을 데이터 클래스
data class PlaceItem(
    val title: String,    // 장소 이름
    val address: String,  // 주소
    val mapx: String,     // 응답 경도 좌표
    val mapy: String      // 응답 위도 좌표
)

// Retrofit이 사용할 API 인터페이스
interface NaverSearchApi {
    @GET("v1/search/local.json")
    suspend fun searchPlaces(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int,
        @Query("coordinate") coordinate: String?
    ): SearchResult
}
