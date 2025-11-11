package com.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.data.db.dao.FavoriteDao
import com.data.db.dao.PlanDao
import com.data.db.dao.UserDao
import com.data.db.entity.FavoriteEntity
import com.data.db.entity.PlanEntity
import com.data.db.entity.PlanMedEntity
import com.data.db.entity.PlanTimeEntity
import com.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        FavoriteEntity::class,
        PlanEntity::class,
        PlanMedEntity::class,
        PlanTimeEntity::class
    ],
    version = 2,                // ← 기존 1에서 올림
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun planDao(): PlanDao

    companion object {
        // 1 -> 2: plans 테이블에 userId 컬럼 및 인덱스 추가
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 컬럼 추가. 기존 데이터는 임시 기본값 ''로 채움
                db.execSQL("ALTER TABLE plans ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                // 인덱스 생성
                db.execSQL("CREATE INDEX IF NOT EXISTS index_plans_userId ON plans(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_plans_createdAt ON plans(createdAt)")
            }
        }
    }
}
