// data/src/main/java/com/data/db/dao/PlanDao.kt
package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT * FROM `plan` WHERE id = :planId")
    fun getPlanById(planId: Long): Flow<PlanEntity?>

    // 🔹 regihistory.userId 를 기준으로 join
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
            SELECT id FROM `regihistory`
            WHERE userId = :userId
        )
        """
    )
    suspend fun deleteAllByUser(userId: Long)

    // 🔥 최근 N일간 복용 완료된 Plan 조회
    @Query(
        """
        SELECT `plan`.*
        FROM `plan`
        INNER JOIN `regihistory`
            ON `plan`.regihistoryId = `regihistory`.id
        WHERE `regihistory`.userId = :userId
        AND `plan`.taken IS NOT NULL
        AND `plan`.takenTime IS NOT NULL
        AND `plan`.exTakenAt IS NOT NULL
        AND `plan`.exTakenAt >= :startTime
        AND `plan`.exTakenAt <= :endTime
        ORDER BY `plan`.exTakenAt DESC
        """
    )
    suspend fun getRecentTakenPlans(
        userId: Long,
        startTime: Long,
        endTime: Long
    ): List<PlanEntity>
}