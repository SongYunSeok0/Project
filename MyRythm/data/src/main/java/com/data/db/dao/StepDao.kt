package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.entity.StepEntity
import com.data.db.entity.DailyStepEntity

@Dao
interface StepDao {

    @Insert
    suspend fun insert(step: StepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStep(step: DailyStepEntity)

    @Query("SELECT * FROM steps")
    suspend fun getAllSteps(): List<StepEntity>

    @Query("DELETE FROM steps")
    suspend fun clearSteps()

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getDailyStep(date: String): DailyStepEntity?

    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT 7")
    suspend fun getLast7Days(): List<DailyStepEntity>
}

