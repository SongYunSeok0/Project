// data/src/main/java/com/data/di/NetworkModule.kt
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
@Qualifier annotation class BufferingInterceptor

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

    // ---- Connection Interceptor ----
    @Provides
    @Singleton
    @ConnectionInterceptor
    fun provideConnectionInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Connection", "close")
            .header("Accept", "application/json")
            .removeHeader("Accept-Encoding")
            .build()

        chain.proceed(request)
    }

    // ---- Buffering Interceptor (에뮬레이터 버그 회피) ----
    @Provides
    @Singleton
    @BufferingInterceptor
    fun provideBufferingInterceptor(): Interceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())

        val responseBody = response.body ?: return@Interceptor response
        val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1

        // Content-Length가 2인 경우 (빈 JSON 배열 "[]")
        if (contentLength == 2L) {
            try {
                val source = responseBody.source()

                // 2바이트 읽기 시도
                if (source.request(2)) {
                    val bytes = source.buffer.readByteArray(2)
                    val newBody = bytes.toResponseBody(responseBody.contentType())

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "  ✅ 버퍼링: 2/2 bytes")
                    }

                    return@Interceptor response.newBuilder()
                        .body(newBody)
                        .build()
                } else {
                    // 읽기 실패 시 빈 배열로 대체
                    Log.w(TAG, "  ⚠️ 2바이트 읽기 실패, 빈 배열 대체")
                    val emptyBody = "[]".toByteArray().toResponseBody(responseBody.contentType())
                    return@Interceptor response.newBuilder()
                        .body(emptyBody)
                        .build()
                }
            } catch (e: ProtocolException) {
                // ProtocolException 발생 시 빈 배열로 대체
                Log.w(TAG, "  ⚠️ ProtocolException, 빈 배열 대체: ${e.message}")
                val emptyBody = "[]".toByteArray().toResponseBody(responseBody.contentType())
                return@Interceptor response.newBuilder()
                    .body(emptyBody)
                    .build()
            } catch (e: Exception) {
                // 기타 예외 시 빈 배열로 대체
                Log.e(TAG, "  ❌ 버퍼링 실패, 빈 배열 대체: ${e.message}")
                val emptyBody = "[]".toByteArray().toResponseBody(responseBody.contentType())
                return@Interceptor response.newBuilder()
                    .body(emptyBody)
                    .build()
            }
        }

        // Content-Length가 3 이상인 경우 일반 버퍼링
        if (contentLength in 3..10_000_000) {
            try {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)
                val bytes = source.buffer.readByteArray()

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "  ✅ 버퍼링: ${bytes.size}/${contentLength} bytes")
                }

                val newBody = bytes.toResponseBody(responseBody.contentType())

                return@Interceptor response.newBuilder()
                    .body(newBody)
                    .header("Content-Length", bytes.size.toString())
                    .build()

            } catch (e: ProtocolException) {
                // 일반 크기 응답의 ProtocolException은 재시도를 위해 예외 전파
                Log.e(TAG, "  ❌ 버퍼링 실패 (재시도 필요): ${e.message}")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "  ❌ 버퍼링 실패: ${e.message}")
                throw e
            }
        }

        response
    }

    // ---- Auth Interceptors ----
    @Provides
    @Singleton
    @NewsAuth
    fun provideNewsAuthInterceptor(): Interceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_NEWS_CLIENT_ID)
            //.addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_NEWS_CLIENT_SECRET)
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
        @BufferingInterceptor bufferingInterceptor: Interceptor,
        @SafeLoggingInterceptor safeLoggingInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(bufferingInterceptor)        // 1️⃣ 버퍼링 (가장 먼저!)
            .addInterceptor(safeLoggingInterceptor)      // 2️⃣ 로깅
            .addInterceptor(connectionInterceptor)        // 3️⃣ Connection 헤더
            .addInterceptor(authHeaderInterceptor)        // 4️⃣ 인증
            .build()

    @Provides
    @Singleton
    @NewsOkHttp
    fun provideNewsOkHttp(
        logging: HttpLoggingInterceptor,
        @NewsAuth newsAuth: Interceptor,
        @ConnectionInterceptor connectionInterceptor: Interceptor,
        @BufferingInterceptor bufferingInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(bufferingInterceptor)
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
        @ConnectionInterceptor connectionInterceptor: Interceptor,
        @BufferingInterceptor bufferingInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(bufferingInterceptor)
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