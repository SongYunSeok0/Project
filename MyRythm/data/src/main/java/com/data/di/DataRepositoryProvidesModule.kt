package com.data.di

import com.data.network.api.MapApi
import com.data.network.api.NewsApi
import com.data.repository.MapRepositoryImpl
import com.data.repository.NewsRepositoryImpl
import com.domain.repository.MapRepository
import com.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataRepositoryProvidesModule {
    @Provides @Singleton
    fun provideNewsRepository(api: NewsApi): NewsRepository =
        NewsRepositoryImpl(getNewsRemote = { api.getNews() })

    @Provides @Singleton
    fun provideMapRepository(api: MapApi): MapRepository =
        MapRepositoryImpl(getMapDataRemote = { api.getMapData() })
}