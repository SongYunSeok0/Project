package com.myrythm.di

import com.data.paging.NaverNewsPagingSource
import com.data.remote.api.NaverNewsApi
import com.data.repository.NaverNewsRepositoryImpl
import com.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://openapi.naver.com/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideNaverNewsApi(retrofit: Retrofit): NaverNewsApi =
        retrofit.create(NaverNewsApi::class.java)

    @Provides
    @Singleton
    fun provideNaverNewsPagingFactory(api: NaverNewsApi): NaverNewsPagingSource.Factory =
        NaverNewsPagingSource.Factory(api)

    @Provides
    @Singleton
    fun provideNaverNewsRepository(factory: NaverNewsPagingSource.Factory): NewsRepository =
        NaverNewsRepositoryImpl(factory)
}
