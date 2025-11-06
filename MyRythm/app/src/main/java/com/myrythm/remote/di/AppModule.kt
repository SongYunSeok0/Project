package com.myrythm.remote.di

import com.data.repository.MapRepositoryImpl
import com.data.repository.NewsRepositoryImpl
import com.myrythm.domain.repository.MapRepository
import com.myrythm.domain.repository.NewsRepository
import com.myrythm.domain.usecase.GetMapDataUseCase
import com.myrythm.domain.usecase.GetNewsUseCase
import com.myrythm.remote.api.MapApi
import com.myrythm.remote.api.NewsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- News ---
    @Provides
    @Singleton
    fun provideNewsRepository(api: NewsApi): NewsRepository =
        NewsRepositoryImpl { api.getNews() }
    @Provides
    @Singleton
    fun provideGetNewsUseCase(repository: NewsRepository): GetNewsUseCase =
        GetNewsUseCase(repository)


    // --- Map ---
    @Provides
    @Singleton
    fun provideMapRepository(api: MapApi): MapRepository =
        MapRepositoryImpl { api.getMapData() }
    @Provides
    @Singleton
    fun provideGetMapDataUseCase(repository: MapRepository): GetMapDataUseCase =
        GetMapDataUseCase(repository)
}