package com.myrythm.remote.di

import com.myrythm.remote.api.NewsApi
import com.data.repository.NewsRepositoryImpl
import com.myrythm.domain.repository.NewsRepository
import com.myrythm.remote.api.MapApi
import com.data.repository.MapRepositoryImpl
import com.myrythm.domain.repository.MapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideNewsRepository(api: NewsApi): NewsRepository {
        return NewsRepositoryImpl(
            getNewsRemote = { api.getNews() } // api.getNews()가 suspend 함수라고 가정
        )
    }
    @Provides
    fun provideMapRepository(api: MapApi): MapRepository {
        return MapRepositoryImpl(
            getMapDataRemote = { api.getMapData() } // suspend 람다로 감싸기
        )
    }
}