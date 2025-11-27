// data/src/main/java/com/data/di/DataRepositoryBindsModule.kt
package com.data.di

import com.data.repository.AuthRepositoryImpl
import com.data.repository.ChatbotRepositoryImpl
import com.data.repository.FavoriteRepositoryImpl
import com.data.repository.HealthRepositoryImpl
import com.data.repository.MediRecordRepositoryImpl
import com.data.repository.PlanRepositoryImpl
import com.data.repository.ProfileRepositoryImpl
import com.data.repository.RegiRepositoryImpl
import com.data.repository.StepRepositoryImpl
import com.data.repository.UserRepositoryImpl
import com.domain.repository.AuthRepository
import com.domain.repository.ChatbotRepository
import com.domain.repository.FavoriteRepository
import com.domain.repository.HealthRepository
import com.domain.repository.MediRecordRepository
import com.domain.repository.PlanRepository
import com.domain.repository.ProfileRepository
import com.domain.repository.RegiRepository
import com.domain.repository.StepRepository
import com.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataRepositoryBindsModule {

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatbotRepository(impl: ChatbotRepositoryImpl): ChatbotRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds @Singleton
    abstract fun bindRegiRepository(impl: RegiRepositoryImpl): RegiRepository

    @Binds
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    @Singleton
    abstract fun bindStepRepository(impl: StepRepositoryImpl): StepRepository

    @Binds
    @Singleton
    abstract fun bindPlanRepository(impl: PlanRepositoryImpl): PlanRepository

    @Binds
    @Singleton
    abstract fun bindMediRecordRepository(impl: MediRecordRepositoryImpl): MediRecordRepository
}
