package com.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.data.db.dao.*
import com.data.db.entity.*

@Database(
    entities = [
        UserEntity::class,
        PrescriptionEntity::class,
        PlanEntity::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun planDao(): PlanDao
    abstract fun prescriptionDao(): PrescriptionDao
}
