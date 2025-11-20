package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE keyword = :keyword")
    suspend fun deleteFavorite(keyword: String)

    @Query("SELECT * FROM favorites ORDER BY lastUsed DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE keyword = :keyword)")
    suspend fun isFavorite(keyword: String): Boolean

    @Query("UPDATE favorites SET lastUsed = :time WHERE keyword = :keyword")
    suspend fun updateLastUsed(keyword: String, time: Long)
}
