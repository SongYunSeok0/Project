package com.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.data.db.dao.FavoriteDao
import com.data.db.dao.InquiryDao
import com.data.db.dao.PlanDao
import com.data.db.dao.RegihistoryDao
import com.data.db.dao.UserDao
import com.data.db.entity.FavoriteEntity
import com.data.db.entity.InquiryEntity
import com.data.db.entity.PlanEntity
import com.data.db.entity.RegihistoryEntity
import com.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        RegihistoryEntity::class,
        PlanEntity::class,
        FavoriteEntity::class,
        InquiryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun planDao(): PlanDao
    abstract fun prescriptionDao(): RegihistoryDao
    abstract fun inquiryDao(): InquiryDao
}
