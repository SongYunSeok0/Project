// data/src/main/java/com/data/repository/StepRepositoryImpl.kt
package com.data.repository

import com.data.db.dao.StepDao
import com.data.db.entity.StepEntity
import com.data.db.entity.DailyStepEntity
import com.data.network.api.StepApi
import com.data.network.dto.step.DailyStepRequest
import com.domain.model.DailyStep
import com.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StepRepositoryImpl @Inject constructor(
    private val dao: StepDao,
    private val api: StepApi
) : StepRepository {

    // 실시간 steps 테이블 저장
    override suspend fun insertStep(steps: Int) {
        dao.insert(
            StepEntity(
                steps = steps
            )
        )
    }

    // 자정 이후 raw steps 정리
    override suspend fun clearSteps() {
        dao.clearSteps()
    }

    // daily_steps 로컬 저장
    override suspend fun saveDailyStep(daily: DailyStep) {
        dao.insertDailyStep(
            DailyStepEntity(
                date = daily.date,
                steps = daily.steps
            )
        )
    }

    // daily_steps 서버 업로드
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

    // 주간 걸음수 그래프용: Room 구독
    override fun observeWeeklySteps(): Flow<List<DailyStep>> {
        return dao.observeLast7Days().map { list ->
            list.map { e ->
                DailyStep(
                    date = e.date,
                    steps = e.steps
                )
            }
        }
    }

    // 주간 걸음수 그래프용: 서버 → Room 동기화
    // 아직 서버 주간 조회 API 없으면 임시로 비워 두고, 나중에 채워도 된다.
    override suspend fun refreshWeeklySteps() {
        runCatching {
            val remote = api.getWeeklySteps()

            val entities = remote.map { dto ->
                DailyStepEntity(
                    date = dto.date,
                    steps = dto.steps
                )
            }

            dao.clearDailySteps()
            dao.insertDailySteps(entities)
        }
    }

    override suspend fun getWeeklySteps(): List<DailyStep> {
        return dao.getLast7Days().map { e ->
            DailyStep(
                date = e.date,
                steps = e.steps
            )
        }
    }
}
