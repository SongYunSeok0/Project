package com.domain.repository

import com.domain.model.DailyStep

interface StepRepository {

    // 🔹 Raw steps: 실시간 기록 저장용
    suspend fun insertStep(steps: Int)

    // 🔹 Raw steps 비우기 (자정 이후 정리용)
    suspend fun clearSteps()

    // 🔹 Daily Step (서버/로컬 요약)
    suspend fun saveDailyStep(daily: DailyStep)

    suspend fun uploadDailyStep(daily: DailyStep)

    suspend fun getWeeklySteps(): List<DailyStep>
}
