package com.data.db.dao

import androidx.room.*
import com.data.db.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM 'plan' WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePlans(userId: Long): Flow<List<PlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: PlanEntity)

    @Update
    suspend fun update(plan: PlanEntity)

    @Query("DELETE FROM 'plan' WHERE id = :planId")
    suspend fun deleteById(planId: Long)

    @Query("DELETE FROM 'plan' WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Long)
}




