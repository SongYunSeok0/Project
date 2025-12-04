package com.data.repository

import com.data.db.dao.PlanDao
import com.data.mapper.toDomainLocal
import com.data.mapper.toEntity
import com.data.network.api.PlanApi
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.mapper.toDomain
import com.data.network.mapper.toUpdateRequest
import com.domain.model.Plan
import com.domain.repository.PlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepositoryImpl @Inject constructor(
    private val dao: PlanDao,
    private val api: PlanApi
) : PlanRepository {

    override fun observePlans(userId: Long): Flow<List<Plan>> =
        dao.getAllByUser(userId).map { list ->
            list.map { it.toDomainLocal() }
        }

    override suspend fun syncPlans(userId: Long) = withContext(Dispatchers.IO) {
        val remotePlans = api.getPlans()
        val domainPlans = remotePlans.map { it.toDomain() }
        val entities = domainPlans.map { it.toEntity() }

        dao.deleteAllByUser(userId)
        dao.insertAll(entities)
    }

    override suspend fun create(
        regihistoryId: Long?,
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Long?,
        useAlarm: Boolean
    ) {
        val body = PlanCreateRequest(
            regihistoryId = regihistoryId,
            medName = medName,
            takenAt = takenAt,
            mealTime = mealTime,
            note = note,
            taken = taken,
            useAlarm = useAlarm
        )
        api.createPlan(body)
    }


    // ✅ [추가] 스마트 일괄 생성 구현
    override suspend fun createPlansSmart(
        regihistoryId: Long,
        startDate: String,
        duration: Int,
        times: List<String>,
        medName: String
    ) {
        // 서버 API 규격에 맞춰 Map으로 데이터를 구성합니다.
        val body = mapOf(
            "regihistoryId" to regihistoryId,
            "startDate" to startDate,
            "duration" to duration,
            "times" to times,
            "medName" to medName
        )

        // API 호출 (PlanApi에 createPlanSmart 함수가 있어야 합니다)
        api.createPlanSmart(body)

    }

    override suspend fun update(userId: Long, plan: Plan) {
        api.updatePlan(plan.id, plan.toUpdateRequest())
        syncPlans(userId)
    }

    override suspend fun delete(userId: Long, planId: Long) {
        api.deletePlan(planId)
        syncPlans(userId)
    }

    override suspend fun markAsTaken(planId: Long): Result<Unit> {
        return runCatching {
            val response = api.markAsTaken(planId)
            if (!response.isSuccessful) {
                throw Exception("복약 완료 실패: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun snoozePlan(planId: Long): Result<Unit> {
        return runCatching {
            val response = api.snoozePlan(planId)
            if (!response.isSuccessful) {
                throw Exception("미루기 실패: ${response.code()} ${response.message()}")
            }
        }
    }

}
