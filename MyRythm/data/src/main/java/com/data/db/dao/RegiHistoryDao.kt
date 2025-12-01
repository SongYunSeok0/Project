package com.data.db.dao

import androidx.room.*
import com.data.db.entity.RegiHistoryEntity
import com.data.db.entity.RegiHistoryWithPlans
import kotlinx.coroutines.flow.Flow

@Dao
interface RegiHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(regihistory: RegiHistoryEntity)

    @Query("SELECT * FROM regihistory ORDER BY id DESC")
    fun getAll(): Flow<List<RegiHistoryEntity>>

    @Transaction
    @Query("SELECT * FROM regihistory WHERE id = :id")
    fun getWithPlans(id: Long): Flow<RegiHistoryWithPlans?>

    @Query("DELETE FROM regihistory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM regihistory WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RegiHistoryEntity>)
}
