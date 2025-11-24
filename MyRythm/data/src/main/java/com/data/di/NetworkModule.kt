// data/src/main/java/com/data/di/NetworkModule.kt
package com.data.di

import com.data.BuildConfig
import com.data.network.api.MapApi
import com.data.network.api.NewsApi
import com.data.network.api.PlanApi
import com.data.network.api.UserApi
import com.data.network.api.ChatbotApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import com.data.core.net.AuthHeaderInterceptor
import com.data.network.api.RegiHistoryApi


// ---- Qualifiers ----
@Qualifier annotation class UserRetrofit
@Qualifier annotation class NewsRetrofit
@Qualifier annotation class MapRetrofit

@Qualifier annotation class UserOkHttp
@Qualifier annotation class NewsOkHttp
@Qualifier annotation class MapOkHttp

@Qualifier annotation class NewsAuth
@Qualifier annotation class MapAuth

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ---- Common ----
    @Provides @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private fun OkHttpClient.Builder.commonTimeouts() = apply {
        connectTimeout(15, TimeUnit.SECONDS)
        readTimeout(120, TimeUnit.SECONDS)
        writeTimeout(120, TimeUnit.SECONDS)
        callTimeout(130, TimeUnit.SECONDS)
    }

    // ---- Auth Interceptors (API Key) ----
    @Provides @Singleton @NewsAuth
    fun provideNewsAuthInterceptor(): Interceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_NEWS_CLIENT_ID)
            .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_NEWS_CLIENT_SECRET)
            .build()
        chain.proceed(req)
    }

    @Provides @Singleton @MapAuth
    fun provideMapAuthInterceptor(): Interceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("X-NCP-APIGW-API-KEY-ID", BuildConfig.NAVER_MAP_CLIENT_ID)
            .addHeader("X-NCP-APIGW-API-KEY", BuildConfig.NAVER_MAP_CLIENT_SECRET)
            .build()
        chain.proceed(req)
    }

    // ---- OkHttp per API ----
    @Provides @Singleton @UserOkHttp
    fun provideUserOkHttp(
        logging: HttpLoggingInterceptor,
        authHeaderInterceptor: AuthHeaderInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(authHeaderInterceptor) // Bearer
            .addInterceptor(logging)
            .build()

    @Provides @Singleton @NewsOkHttp
    fun provideNewsOkHttp(
        logging: HttpLoggingInterceptor,
        @NewsAuth newsAuth: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(newsAuth)
            .addInterceptor(logging)
            .build()

    @Provides @Singleton @MapOkHttp
    fun provideMapOkHttp(
        logging: HttpLoggingInterceptor,
        @MapAuth mapAuth: Interceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .commonTimeouts()
            .addInterceptor(mapAuth)
            .addInterceptor(logging)
            .build()

    // ---- Retrofit per API ----
    @Provides @Singleton @UserRetrofit
    fun provideUserRetrofit(@UserOkHttp ok: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(ok)
            .build()

    @Provides @Singleton @NewsRetrofit
    fun provideNewsRetrofit(@NewsOkHttp ok: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.NAVER_NEWS_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(ok)
            .build()

    @Provides @Singleton @MapRetrofit
    fun provideMapRetrofit(@MapOkHttp ok: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.NAVER_MAP_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(ok)
            .build()

    // ---- APIs ----
    @Provides @Singleton
    fun provideUserApi(@UserRetrofit retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)


    @Provides @Singleton
    fun provideMapApi(@MapRetrofit retrofit: Retrofit): MapApi =
        retrofit.create(MapApi::class.java)

    @Provides @Singleton
    fun providePlanApi(@UserRetrofit retrofit: Retrofit): PlanApi =
        retrofit.create(PlanApi::class.java)

    @Provides @Singleton
    fun provideChatbotApi(@UserRetrofit retrofit: Retrofit): ChatbotApi =
        retrofit.create(ChatbotApi::class.java)

    @Provides
    @Singleton
    fun provideNewsApi(@NewsRetrofit retrofit: Retrofit): NewsApi =
        retrofit.create(NewsApi::class.java)

    @Provides
    @Singleton
    fun provideRegiHistoryApi(
        @UserRetrofit retrofit: Retrofit
    ): RegiHistoryApi = retrofit.create(RegiHistoryApi::class.java)
}
