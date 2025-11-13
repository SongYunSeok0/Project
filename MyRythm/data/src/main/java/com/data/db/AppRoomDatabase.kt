package com.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.data.db.dao.FavoriteDao
import com.data.db.dao.InquiryDao
import com.data.db.dao.PlanDao
import com.data.db.dao.UserDao
import com.data.db.entity.FavoriteEntity
import com.data.db.entity.InquiryEntity
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
        PlanTimeEntity::class,
        InquiryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun planDao(): PlanDao

    abstract fun inquiryDao(): InquiryDao
}
