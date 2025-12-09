package com.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.data.db.dao.FavoriteDao
import com.data.db.dao.HeartRateDao
import com.data.db.dao.InquiryDao
import com.data.db.dao.PlanDao
import com.data.db.dao.RegiHistoryDao
import com.data.db.dao.StepDao
import com.data.db.dao.UserDao
import com.data.db.entity.DailyStepEntity
import com.data.db.entity.FavoriteEntity
import com.data.db.entity.HeartRateEntity
import com.data.db.entity.InquiryCommentEntity
import com.data.db.entity.InquiryEntity
import com.data.db.entity.PlanEntity
import com.data.db.entity.RegiHistoryEntity
import com.data.db.entity.StepEntity
import com.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        RegiHistoryEntity::class,
        PlanEntity::class,
        FavoriteEntity::class,
        InquiryEntity::class,
        InquiryCommentEntity::class,
        StepEntity::class,
        DailyStepEntity::class,
        HeartRateEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun planDao(): PlanDao
    abstract fun regiHistoryDao(): RegiHistoryDao
    abstract fun inquiryDao(): InquiryDao
    abstract fun stepDao(): StepDao
    abstract fun heartRateDao(): HeartRateDao

    companion object {

        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getInstance(context: Context): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "app.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}


