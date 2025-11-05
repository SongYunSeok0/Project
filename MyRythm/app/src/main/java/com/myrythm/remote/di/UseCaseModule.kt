package com.myrythm.remote.di


import com.myrythm.domain.repository.NewsRepository
import com.myrythm.domain.usecase.GetNewsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetNewsUseCase(
        repository: NewsRepository
    ): GetNewsUseCase = GetNewsUseCase(repository)
}
