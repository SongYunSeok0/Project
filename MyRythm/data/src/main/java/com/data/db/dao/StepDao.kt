package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.entity.StepEntity
import com.data.db.entity.DailyStepEntity

@Dao
interface StepDao {

    // 실시간 스냅샷 저장
    @Insert
    suspend fun insert(step: StepEntity)

    // 하루 총 걸음수 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStep(step: DailyStepEntity)

    // 최근 7일 걸음수
    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT 7")
    suspend fun getLast7Days(): List<DailyStepEntity>

    // 특정 날짜 조회 (있으면 업데이트용)
    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getDailyStep(date: String): DailyStepEntity?
}
