package com.data.core.di

import android.content.Context
import com.data.core.auth.EncryptedPrefsTokenStore
import com.data.core.auth.TokenStore
import com.data.core.net.AuthHeaderInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreAuthModule {

    @Provides
    @Singleton
    fun provideCoreScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideTokenStore(
        @ApplicationContext ctx: Context
    ): TokenStore = EncryptedPrefsTokenStore(ctx)

    @Provides
    @Singleton
    fun provideAuthHeaderInterceptor(
        tokenStore: TokenStore
    ): AuthHeaderInterceptor = AuthHeaderInterceptor(tokenStore)
}
