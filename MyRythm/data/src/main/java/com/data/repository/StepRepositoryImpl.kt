package com.data.repository

import com.data.db.dao.StepDao
import com.data.db.entity.StepEntity
import com.data.db.entity.DailyStepEntity
import com.data.network.api.StepApi
import com.data.network.dto.step.DailyStepRequest
import com.domain.model.DailyStep
import com.domain.repository.StepRepository
import javax.inject.Inject

class StepRepositoryImpl @Inject constructor(
    private val dao: StepDao,
    private val api: StepApi
) : StepRepository {

    // 🔥 실시간 steps 테이블 저장 (collectedAt 은 여기서 현재 시각 사용)
    override suspend fun insertStep(steps: Int) {
        dao.insert(
            StepEntity(
                steps = steps
            )
        )
    }

    // 🔥 자정 이후 raw steps 정리용
    override suspend fun clearSteps() {
        dao.clearSteps()
    }

    // 🔥 daily_steps 저장 + 서버 업로드 (요약 데이터용)
    override suspend fun saveDailyStep(daily: DailyStep) {
        dao.insertDailyStep(
            DailyStepEntity(
                date = daily.date,
                steps = daily.steps
            )
        )

        runCatching {
            api.uploadDailyStep(
                DailyStepRequest(
                    date = daily.date,
                    steps = daily.steps
                )
            )
        }
    }

    override suspend fun uploadDailyStep(daily: DailyStep) {
        runCatching {
            api.uploadDailyStep(
                DailyStepRequest(
                    date = daily.date,
                    steps = daily.steps
                )
            )
        }
    }

    override suspend fun getWeeklySteps(): List<DailyStep> {
        return dao.getLast7Days().map {
            DailyStep(
                date = it.date,
                steps = it.steps
            )
        }
    }
}
