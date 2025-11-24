package com.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.data.db.dao.FavoriteDao
import com.data.db.dao.InquiryDao
import com.data.db.dao.PlanDao
import com.data.db.dao.PrescriptionDao
import com.data.db.dao.StepDao
import com.data.db.dao.UserDao
import com.data.db.entity.DailyStepEntity
import com.data.db.entity.FavoriteEntity
import com.data.db.entity.InquiryEntity
import com.data.db.entity.PlanEntity
import com.data.db.entity.PrescriptionEntity
import com.data.db.entity.StepEntity
import com.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PrescriptionEntity::class,
        PlanEntity::class,
        FavoriteEntity::class,
        InquiryEntity::class,
        StepEntity::class,
        DailyStepEntity::class,
    ],
    version = 5,
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun planDao(): PlanDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun inquiryDao(): InquiryDao
    abstract fun stepDao(): StepDao
}
