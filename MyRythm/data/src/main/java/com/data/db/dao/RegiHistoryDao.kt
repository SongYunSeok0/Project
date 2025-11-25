package com.data.db.dao

import androidx.room.*
import com.data.db.entity.PlanEntity
import com.data.db.entity.RegiHistoryEntity
import com.data.db.entity.RegiHistoryWithPlans
import kotlinx.coroutines.flow.Flow

@Dao
interface RegiHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(regiHistory: RegiHistoryEntity)

    @Query("SELECT * FROM regihistory ORDER BY id DESC")
    fun getAll(): Flow<List<RegiHistoryEntity>>

    @Transaction
    @Query("SELECT * FROM regihistory WHERE id = :id")
    fun getWithPlans(id: Long): Flow<RegiHistoryWithPlans?>
}


