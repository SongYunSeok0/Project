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

    @Query("SELECT * FROM `plan` WHERE regihistoryId = :regihistoryId")
    fun getByRegiHistory(regihistoryId: Long): Flow<List<PlanEntity>>

    @Query(
        """
        SELECT `plan`.* 
        FROM `plan`
        INNER JOIN `regihistory`
            ON `plan`.regihistoryId = `regihistory`.id
        WHERE `regihistory`.userId = :userId
        """
    )
    fun getAllByUser(userId: Long): Flow<List<PlanEntity>>

    @Query(
        """
        DELETE FROM `plan`
        WHERE regihistoryId IN (
            SELECT id FROM `regihistory` WHERE userId = :userId
        )
        """
    )
    suspend fun deleteAllByUser(userId: Long)
}
