package com.domain.repository

import com.domain.model.DailyStep   // ★ 이거 추가

interface StepRepository {
    suspend fun saveSnapshot(steps: Int, collectedAt: Long)

    // 하루 총 걸음수 저장
    suspend fun saveDailyStep(date: String, steps: Int)

    // 최근 7일 걸음수
    suspend fun getWeeklySteps(): List<DailyStep>
}
