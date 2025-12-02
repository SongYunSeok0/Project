package com.data.di

import com.data.network.api.MapApi
import com.data.network.api.NewsApi
import com.data.network.api.DeviceApi
import com.data.repository.MapRepositoryImpl
import com.data.repository.NewsRepositoryImpl
import com.data.repository.DeviceRepositoryImpl
import com.domain.repository.MapRepository
import com.domain.repository.NewsRepository
import com.domain.repository.DeviceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    @Singleton
    fun provideDeviceRepository(api: DeviceApi): DeviceRepository =
        DeviceRepositoryImpl(api)
}