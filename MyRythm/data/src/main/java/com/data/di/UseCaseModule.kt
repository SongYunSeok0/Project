package com.data.di

import com.domain.repository.AuthRepository
import com.domain.repository.UserRepository
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.RefreshTokenUseCase
import com.domain.usecase.user.SignupUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
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
    fun provideSignupUseCase(repo: UserRepository) =
        SignupUseCase(repo)

    @Provides
    @Singleton
    fun provideRegisterFcmTokenUseCase(
        userRepository: UserRepository
    ) = RegisterFcmTokenUseCase(userRepository)
}
