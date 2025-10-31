package com.map.data

import com.map.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API 응답 구조에 맞는 데이터 클래스
data class SearchResult(
    val lastBuildDate: String? = null,
    val total: Int = 0,
    val start: Int = 1,
    val display: Int = 0,
    val items: List<PlaceItem> = emptyList()
)

// ✅ category 필드 추가
data class PlaceItem(
    val title: String,
    val link: String? = null,
    val category: String? = null,
    val telephone: String? = null,
    val address: String,
    val roadAddress: String? = null,
    val mapx: String,
    val mapy: String
)

// Retrofit이 사용할 API 인터페이스
interface NaverSearchApi {
    @GET("v1/search/local.json")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("display") display: Int = 50,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim"
    ): SearchResult
}

private class NaverHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_CLIENT_ID)
            .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_CLIENT_SECRET)
            .build()
        return chain.proceed(req)
    }
}

object NaverSearchService {
    private val client = OkHttpClient.Builder()
        .addInterceptor(NaverHeaderInterceptor())
        .build()

    val api: NaverSearchApi = Retrofit.Builder()
        .baseUrl("https://openapi.naver.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(NaverSearchApi::class.java)
}
