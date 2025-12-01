package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.entity.StepEntity
import com.data.db.entity.DailyStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    // 실시간 step 저장
    @Insert
    suspend fun insert(step: StepEntity)

    // 실시간 step 비우기
    @Query("DELETE FROM steps")
    suspend fun clearSteps()

    // 일일 요약 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStep(daily: DailyStepEntity)

    // 기존 주간 조회 함수 (suspend 버전)
    @Query("""
        SELECT * FROM daily_steps
        ORDER BY date DESC
        LIMIT 7
    """)
    suspend fun getLast7Days(): List<DailyStepEntity>

    // 그래프용: Flow로 관찰
    @Query("""
        SELECT * FROM daily_steps
        ORDER BY date DESC
        LIMIT 7
    """)
    fun observeLast7Days(): Flow<List<DailyStepEntity>>

    // 서버에서 다시 내려받아 덮어쓸 때 사용
    @Query("DELETE FROM daily_steps")
    suspend fun clearDailySteps()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySteps(list: List<DailyStepEntity>)
}
