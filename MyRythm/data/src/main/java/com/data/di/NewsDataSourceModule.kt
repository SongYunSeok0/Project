package com.data.di

import com.data.network.api.NewsApi
import com.data.network.datasource.NewsHtmlParser
import com.data.network.datasource.NewsRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NewsDataSourceModule {

    @Provides
    @Singleton
    fun provideNewsRemoteDataSource(
        api: NewsApi
    ): NewsRemoteDataSource =
        NewsRemoteDataSource(api)

    @Provides
    @Singleton
    fun provideNewsHtmlParser(): NewsHtmlParser =
        NewsHtmlParser()
}