package com.data.di

import android.util.Log
import com.data.BuildConfig
import com.data.network.api.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.ProtocolException
import java.util.concurrent.TimeUnit
import com.data.core.net.AuthHeaderInterceptor

// ---- Qualifiers ----
@Qualifier annotation class UserRetrofit
@Qualifier annotation class NewsRetrofit
@Qualifier annotation class MapRetrofit
@Qualifier annotation class UserOkHttp
@Qualifier annotation class NewsOkHttp
@Qualifier annotation class MapOkHttp
@Qualifier annotation class NewsAuth
@Qualifier annotation class MapAuth
@Qualifier annotation class ConnectionInterceptor
@Qualifier annotation class SafeLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TAG = "NetworkModule"

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    private fun OkHttpClient.Builder.commonTimeouts() = apply {
        connectTimeout(15, TimeUnit.SECONDS)
        readTimeout(120, TimeUnit.SECONDS)
        writeTimeout(120, TimeUnit.SECONDS)
        callTimeout(130, TimeUnit.SECONDS)
        retryOnConnectionFailure(true)

        // 에뮬레이터: 연결 재사용 비활성화
        connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))

        // HTTP/1.1만 사용
        protocols(listOf(Protocol.HTTP_1_1))
    }

    // ---- Safe Logging Interceptor ----
    @Provides
    @Singleton
    @SafeLoggingInterceptor
    fun provideSafeLoggingInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "→ ${request.method} ${request.url}")
        }

        try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "← ${response.code} ${request.url.encodedPath} (${duration}ms)")
                response.header("Content-Length")?.let {
                    Log.d(TAG, "  Content-Length: $it bytes")
                }
            }

            response
        } catch (e: ProtocolException) {
            val duration = System.currentTimeMillis() - startTime
            Log.e(TAG, "✗ ProtocolException (${duration}ms)")
            Log.e(TAG, "  URL: ${request.url}")
            Log.e(TAG, "  Error: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Log.e(TAG, "✗ ${e.javaClass.simpleName} (${duration}ms): ${e.message}")
            throw e
        }
    }

    // ---- Connection Interceptor (응답 버퍼링) ----
    @Provides
    @Singleton
    @ConnectionInterceptor
    fun provideConnectionInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Connection", "close")
            .header("Accept", "application/json")
            .removeHeader("Accept-Encoding")
            .build()

        try {
            val response = chain.proceed(request)

            // ✅ 응답 본문을 미리 완전히 읽어서 버퍼링
            val responseBody = response.body
            if (responseBody != null) {
                val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1

                // Content-Length가 있는 응답만 버퍼링 (10MB 미만)
                if (contentLength in 0..10_000_000) {
                    try {
                        val source = responseBody.source()
                        source.request(Long.MAX_VALUE)  // 전체 읽기
                        val bytes = source.buffer.readByteArray()

                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "  ✅ 버퍼링: ${bytes.size}/${contentLength} bytes")
                        }

                        // 새로운 ResponseBody로 교체
                        val newBody = bytes.toResponseBody(responseBody.contentType())

                        return@Interceptor response.newBuilder()
                            .body(newBody)
                            .header("Content-Length", bytes.size.toString())
                            .build()

                    } catch (e: Exception) {
                        Log.e(TAG, "버퍼링 실패: ${e.message}", e)
                    }
                }
            }

            response

        } catch (e: Exception) {
            Log.e(TAG, "요청 실패: ${e.message}", e)
            throw e
        }
    }

    // ---- Auth Interceptors ----
    @Provides
    @Singleton
    @NewsAuth
    fun provideNewsAuthInterceptor(): Interceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_NEWS_CLIENT_ID)
            .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_NEWS_CLIENT_SECRET)
            .build()
        chain.proceed(req)
    }

    @Provides
    @Singleton
    @MapAuth
    fun provideMapAuthInterceptor(): Interceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("X-NCP-APIGW-API-KEY-ID", BuildConfig.NAVER_MAP_CLIENT_ID)
            .addHeader("X-NCP-APIGW-API-KEY", BuildConfig.NAVER_MAP_CLIENT_SECRET)
            .build()
        chain.proceed(req)
    }

    // ---- OkHttp per API ----
    @Provides
    @Singleton
    @UserOkHttp
    fun provideUserOkHttp(
        authHeaderInterceptor: AuthHeaderInterceptor,
        @ConnectionInterceptor connectionInterceptor: Interceptor,
        @SafeLoggingInterceptor safeLoggingInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(safeLoggingInterceptor)         // 1️⃣ 로깅
            .addInterceptor(connectionInterceptor)           // 2️⃣ 버퍼링 (핵심!)
            .addInterceptor(authHeaderInterceptor)           // 3️⃣ 인증
            .build()

    @Provides
    @Singleton
    @NewsOkHttp
    fun provideNewsOkHttp(
        logging: HttpLoggingInterceptor,
        @NewsAuth newsAuth: Interceptor,
        @ConnectionInterceptor connectionInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(connectionInterceptor)
            .addInterceptor(newsAuth)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @MapOkHttp
    fun provideMapOkHttp(
        logging: HttpLoggingInterceptor,
        @MapAuth mapAuth: Interceptor,
        @ConnectionInterceptor connectionInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(connectionInterceptor)
            .addInterceptor(mapAuth)
            .addInterceptor(logging)
            .build()

    // ---- Retrofit per API ----
    @Provides
    @Singleton
    @UserRetrofit
    fun provideUserRetrofit(@UserOkHttp ok: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(ok)
            .build()

    @Provides
    @Singleton
    @NewsRetrofit
    fun provideNewsRetrofit(@NewsOkHttp ok: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.NAVER_NEWS_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(ok)
            .build()

    @Provides
    @Singleton
    @MapRetrofit
    fun provideMapRetrofit(@MapOkHttp ok: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.NAVER_MAP_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(ok)
            .build()

    // ---- APIs ----
    @Provides
    @Singleton
    fun provideUserApi(@UserRetrofit retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideMapApi(@MapRetrofit retrofit: Retrofit): MapApi =
        retrofit.create(MapApi::class.java)

    @Provides
    @Singleton
    fun providePlanApi(@UserRetrofit retrofit: Retrofit): PlanApi =
        retrofit.create(PlanApi::class.java)

    @Provides
    @Singleton
    fun provideChatbotApi(@UserRetrofit retrofit: Retrofit): ChatbotApi =
        retrofit.create(ChatbotApi::class.java)

    @Provides
    @Singleton
    fun provideNewsApi(@NewsRetrofit retrofit: Retrofit): NewsApi =
        retrofit.create(NewsApi::class.java)

    @Provides
    @Singleton
    fun provideRegiHistoryApi(@UserRetrofit retrofit: Retrofit): RegiHistoryApi =
        retrofit.create(RegiHistoryApi::class.java)

    @Provides
    fun provideHealthApi(@UserRetrofit retrofit: Retrofit): HeartRateApi =
        retrofit.create(HeartRateApi::class.java)

    @Provides
    @Singleton
    fun provideStepApi(@UserRetrofit retrofit: Retrofit): StepApi =
        retrofit.create(StepApi::class.java)

    @Provides
    @Singleton
    fun provideDeviceApi(@UserRetrofit retrofit: Retrofit): DeviceApi =
        retrofit.create(DeviceApi::class.java)

    @Provides
    @Singleton
    fun provideInquiryApi(@UserRetrofit retrofit: Retrofit): InquiryApi =
        retrofit.create(InquiryApi::class.java)
}