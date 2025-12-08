package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.entity.HeartRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<HeartRateEntity>)

    @Query("SELECT * FROM heart_rate ORDER BY collectedAt DESC")
    fun getAll(): Flow<List<HeartRateEntity>>

    @Query("DELETE FROM heart_rate")
    suspend fun clear()

    // 🔥 최근 7일치 데이터 (suspend 함수)
    @Query("""
        SELECT * FROM heart_rate 
        WHERE collectedAt >= :timestamp 
        ORDER BY collectedAt DESC
    """)
    suspend fun getLastWeek(timestamp: Long): List<HeartRateEntity>
}