package com.data.di

import com.data.core.auth.AuthTokenProviderImpl
import com.domain.usecase.auth.AuthTokenProvider
import com.domain.usecase.auth.GetAuthStatusUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthTokenProvider(
        impl: AuthTokenProviderImpl
    ): AuthTokenProvider

}