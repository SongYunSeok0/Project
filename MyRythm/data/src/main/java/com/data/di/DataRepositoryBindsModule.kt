package com.data.di

import com.data.repository.FavoriteRepositoryImpl
import com.data.repository.PlanRepositoryImpl
import com.data.repository.UserRepositoryImpl
import com.domain.repository.FavoriteRepository
import com.domain.repository.PlanRepository
import com.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataRepositoryBindsModule {
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds @Singleton
    abstract fun bindPlanRepository(impl: PlanRepositoryImpl): PlanRepository
}
