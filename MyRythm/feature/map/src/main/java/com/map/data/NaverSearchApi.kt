package com.map.data

import com.map.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

//dataclass는 data모듈의 remote/dto로 옮겨야 함
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

//data모듈의 remote/api로 옮겨야함
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

//data 모듈의 remote/network로 옮겨야함
private class NaverHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_CLIENT_ID)
            .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_CLIENT_SECRET)
            .build()
        return chain.proceed(req)
    }
}

//data 모듈의 remote/di로 옮기거나 Hilt써야함 di로 옮기던 hilt을 쓰던 news랑 map이랑 공유하도록
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
