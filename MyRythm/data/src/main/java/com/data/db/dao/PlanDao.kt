package com.data.db.dao

import androidx.room.*
import com.data.db.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM 'plan'")
    fun observePlans(): Flow<List<PlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PlanEntity)

    @Update
    suspend fun update(plan: PlanEntity)

    @Query("DELETE FROM 'plan' WHERE id = :planId")
    suspend fun deleteById(planId: Long)

    @Query("DELETE FROM 'plan'")
    suspend fun deleteAllByUser()

    @Query("SELECT * FROM 'plan'")
    suspend fun getAllOnce(): List<PlanEntity>
}




