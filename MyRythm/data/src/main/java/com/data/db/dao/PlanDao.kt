package com.data.db.dao

import androidx.room.*
import com.data.db.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: PlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<PlanEntity>)

    @Query("SELECT * FROM 'plan' WHERE regiHistoryId = :regiHistoryId")
    fun getByRegiHistory(regiHistoryId: Long): Flow<List<PlanEntity>>

    @Query(
        """
        SELECT `plan`.* 
        FROM `plan`
        INNER JOIN `regiHistory`
            ON `plan`.regiHistoryId = `regiHistory`.id
        WHERE `regiHistory`.userId = :userId
    """
    )
    fun getAllByUser(userId: Long): Flow<List<PlanEntity>>


}


