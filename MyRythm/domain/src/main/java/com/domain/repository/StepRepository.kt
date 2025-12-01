// domain/src/main/java/com/domain/repository/StepRepository.kt
package com.domain.repository

import com.domain.model.DailyStep
import kotlinx.coroutines.flow.Flow

interface StepRepository {

    // 실시간 누적용 (StepViewModel에서 사용 중)
    suspend fun insertStep(steps: Int)
    suspend fun clearSteps()
    suspend fun saveDailyStep(daily: DailyStep)
    suspend fun uploadDailyStep(daily: DailyStep)

    // 그래프용
    fun observeWeeklySteps(): Flow<List<DailyStep>>
    suspend fun refreshWeeklySteps()
}
