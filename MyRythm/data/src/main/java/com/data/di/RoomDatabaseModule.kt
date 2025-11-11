package com.data.di

import android.content.Context
import androidx.room.Room
import com.data.db.AppRoomDatabase
import com.data.db.dao.UserDao
import com.data.db.dao.FavoriteDao
import com.data.db.dao.PlanDao
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
            .addMigrations(AppRoomDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideUserDao(db: AppRoomDatabase): UserDao = db.userDao()
    @Provides fun provideFavoriteDao(db: AppRoomDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun providePlanDao(db: AppRoomDatabase): PlanDao = db.planDao()
}
