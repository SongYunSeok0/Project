package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.data.db.entity.PlanEntity
import com.data.db.entity.PlanMedEntity
import com.data.db.entity.PlanTimeEntity
import com.data.db.entity.PlanWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Transaction
    @Query("SELECT * FROM plans WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePlans(userId: String): Flow<List<PlanWithDetails>>

    @Transaction
    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlan(id: Long): PlanWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeds(meds: List<PlanMedEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimes(times: List<PlanTimeEntity>)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deletePlan(id: Long)

    @Query("DELETE FROM plans WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Transaction
    suspend fun insertAll(
        plan: PlanEntity,          // ← userId 포함된 엔티티 그대로 받음
        meds: List<String>,
        times: List<String>
    ): Long {
        val planId = insertPlan(plan)  // ← copy(userId=…) 제거

        if (meds.isNotEmpty()) {
            insertMeds(
                meds.filter { it.isNotBlank() }
                    .map { name -> PlanMedEntity(planId = planId, name = name.trim()) }
            )
        }

        insertTimes(
            times.mapIndexed { idx, time ->
                PlanTimeEntity(planId = planId, orderIndex = idx, hhmm = time)
            }
        )

        return planId
    }
}
