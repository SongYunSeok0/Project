package com.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.data.db.dao.FavoriteDao
import com.data.db.dao.InquiryDao
import com.data.db.dao.PlanDao
import com.data.db.dao.RegiHistoryDao
import com.data.db.dao.UserDao
import com.data.db.entity.FavoriteEntity
import com.data.db.entity.InquiryEntity
import com.data.db.entity.PlanEntity
import com.data.db.entity.RegiHistoryEntity
import com.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        RegiHistoryEntity::class,
        PlanEntity::class,
        FavoriteEntity::class,
        InquiryEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun planDao(): PlanDao
    abstract fun regiHistoryDao(): RegiHistoryDao
    abstract fun inquiryDao(): InquiryDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ⚠️ 테이블 삭제 후 재생성 or 컬럼 추가 등 필요한 처리
            }
        }

        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getInstance(context: Context): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "app.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // 구조 완전 바뀐 경우 이거 추천
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}


