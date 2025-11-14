package com.data.di

import android.content.Context
import androidx.room.Room
import com.data.db.AppRoomDatabase
import com.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomDatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppRoomDatabase =
        Room.databaseBuilder(ctx, AppRoomDatabase::class.java, "app.db")
            .fallbackToDestructiveMigration() // 버전 바뀌면 DB 재생성
            .build()

    // ✅ 각 Dao 제공
    @Provides fun provideUserDao(db: AppRoomDatabase): UserDao = db.userDao()
    @Provides fun provideFavoriteDao(db: AppRoomDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun providePlanDao(db: AppRoomDatabase): PlanDao = db.planDao()
    @Provides fun providePrescriptionDao(db: AppRoomDatabase): PrescriptionDao = db.prescriptionDao()
}
