//package com.auth.data.local
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//
//@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
//abstract class FavoriteDatabase : RoomDatabase() {
//    abstract fun favoriteDao(): FavoriteDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: FavoriteDatabase? = null
//
//        fun getInstance(context: Context): FavoriteDatabase {
//            return INSTANCE ?: synchronized(this) {
//                INSTANCE ?: Room.databaseBuilder(
//                    context.applicationContext,
//                    FavoriteDatabase::class.java,
//                    "auth_favorites.db"
//                ).build().also { INSTANCE = it }
//            }
//        }
//    }
//}
