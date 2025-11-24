package com.data.repository

import com.data.db.dao.StepDao
import com.data.db.entity.DailyStepEntity
import com.data.db.entity.StepEntity
import com.data.network.api.StepApi
import com.data.network.dto.step.DailyStepRequest
import com.data.network.dto.step.StepCountRequest
import com.domain.model.DailyStep
import com.domain.repository.StepRepository
import javax.inject.Inject

class StepRepositoryImpl @Inject constructor(
    private val dao: StepDao,
    private val api: StepApi
) : StepRepository {

    override suspend fun saveSnapshot(steps: Int, collectedAt: Long) {
        // 로컬 저장
        dao.insert(
            StepEntity(
                steps = steps,
                collectedAt = collectedAt
            )
        )

        // 서버 스냅샷 업로드 (실패해도 앱 죽지 않게)
        runCatching {
            api.uploadStepCount(
                StepCountRequest(
                    steps = steps,
                    collected_at = collectedAt
                )
            )
        }
    }

    override suspend fun saveDailyStep(date: String, steps: Int) {
        // 로컬 저장 (하루당 1개 덮어쓰기)
        dao.insertDailyStep(
            DailyStepEntity(
                date = date,
                steps = steps
            )
        )

        // 서버 업로드 (실패해도 무시)
        runCatching {
            api.uploadDailyStep(
                DailyStepRequest(
                    date = date,
                    steps = steps
                )
            )
        }
    }

    override suspend fun getWeeklySteps(): List<DailyStep> {
        return dao.getLast7Days().map { entity ->
            DailyStep(
                date = entity.date,
                steps = entity.steps
            )
        }
    }
}
