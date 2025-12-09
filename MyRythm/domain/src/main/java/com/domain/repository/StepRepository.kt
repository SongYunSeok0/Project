package com.domain.repository

import com.domain.model.DailyStep
import kotlinx.coroutines.flow.Flow

interface StepRepository {

    suspend fun insertStep(steps: Int)
    suspend fun clearSteps()
    suspend fun saveDailyStep(daily: DailyStep)
    suspend fun uploadDailyStep(daily: DailyStep)

    // 그래프용
    fun observeWeeklySteps(): Flow<List<DailyStep>>
    suspend fun refreshWeeklySteps()
    suspend fun getWeeklySteps(): List<DailyStep>

    suspend fun insertDummyData()
}
