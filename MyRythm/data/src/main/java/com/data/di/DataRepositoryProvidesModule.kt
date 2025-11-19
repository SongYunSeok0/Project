package com.data.di

import com.data.db.dao.InquiryDao
import com.data.network.api.ChatbotApi
import com.data.network.api.MapApi
import com.data.network.api.NewsApi
import com.data.repository.ChatbotRepositoryImpl
import com.data.repository.InquiryRepositoryImpl
import com.data.repository.MapRepositoryImpl
import com.data.repository.NewsRepositoryImpl
import com.domain.repository.ChatbotRepository
import com.domain.repository.InquiryRepository
import com.domain.repository.MapRepository
import com.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataRepositoryProvidesModule {
    @Provides
    @Singleton
    fun provideNewsRepository(
        api: NewsApi
    ): NewsRepository = NewsRepositoryImpl(api)

    @Provides @Singleton
    fun provideMapRepository(api: MapApi): MapRepository =
        MapRepositoryImpl(getMapDataRemote = { api.getMapData() })

    @Provides
    fun provideInquiryRepository(dao: InquiryDao): InquiryRepository =
        InquiryRepositoryImpl(dao)
}