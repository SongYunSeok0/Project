package com.myrhythm.di

import android.content.Context
import com.data.core.push.FcmTokenStore
import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import com.domain.repository.HealthRepository
import com.domain.repository.PushRepository
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.RefreshTokenUseCase
import com.domain.usecase.user.SignupUseCase
import com.domain.usecase.health.GetHeartHistoryUseCase
import com.domain.usecase.health.GetLatestHeartRateUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase   // ← 이거 추가
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository
    ) = LoginUseCase(authRepository, userRepository)

    @Provides
    @Singleton
    fun provideRefreshTokenUseCase(repo: AuthRepository) =
        RefreshTokenUseCase(repo)

    @Provides
    @Singleton
    fun provideLogoutUseCase(repo: AuthRepository) =
        LogoutUseCase(repo)

    @Provides
    @Singleton
    fun provideSignupUseCase(repo: AuthRepository) =
        SignupUseCase(repo)

    @Provides
    @Singleton
    fun provideRegisterFcmTokenUseCase(
        pushRepository: PushRepository
    ): RegisterFcmTokenUseCase = RegisterFcmTokenUseCase(pushRepository)

    @Provides
    @Singleton
    fun provideFcmTokenStore(
        @ApplicationContext context: Context
    ): FcmTokenStore = FcmTokenStore(context)

    @Provides
    @Singleton
    fun provideGetLatestHeartRateUseCase(
        healthRepository: HealthRepository
    ) = GetLatestHeartRateUseCase(healthRepository)

    @Provides
    @Singleton
    fun provideGetHeartHistoryUseCase(
        healthRepository: HealthRepository
    ) = GetHeartHistoryUseCase(healthRepository)
}