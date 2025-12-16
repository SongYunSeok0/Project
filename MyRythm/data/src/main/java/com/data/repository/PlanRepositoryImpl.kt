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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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

    override fun getPlanById(planId: Long): Flow<Plan?> {
        return dao.getPlanById(planId)
            .map { entity -> entity?.toDomainLocal() }
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
        taken: Boolean?,
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

    override suspend fun getRecentTakenPlans(userId: Long, days: Int): List<Plan> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)  // ✅ yesterday → today로 변경

        // 오늘 23:59:59 (현재 시각까지)
        val endTime = today.atTime(LocalTime.now(zone))  // ✅ 현재 시각까지
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        // (days)일 전 00:00:00
        val startTime = today.minusDays((days - 1).toLong())
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        android.util.Log.d("PlanRepo", "조회 범위: ${java.util.Date(startTime)} ~ ${java.util.Date(endTime)}")

        val entities = dao.getRecentTakenPlans(userId, startTime, endTime)

        android.util.Log.d("PlanRepo", "조회된 데이터: ${entities.size}개")

        return entities.map { entity ->
            entity.toDomainLocal()
        }
    }
}