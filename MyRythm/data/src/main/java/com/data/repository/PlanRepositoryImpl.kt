package com.data.repository

import com.data.db.dao.PlanDao
import com.data.mapper.toDomainLocal
import com.data.mapper.toEntity
import com.data.network.api.PlanApi
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.mapper.toDomain
import com.data.network.mapper.toUpdateRequest
import com.data.util.apiResultOf
import com.domain.model.ApiResult
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

    override suspend fun syncPlans(userId: Long): ApiResult<Unit> =
        withContext(Dispatchers.IO) {
            apiResultOf {
                val remotePlans = api.getPlans()
                val domainPlans = remotePlans.map { it.toDomain() }
                val entities = domainPlans.map { it.toEntity() }

                dao.deleteAllByUser(userId)
                dao.insertAll(entities)
            }
        }

    override suspend fun create(
        regihistoryId: Long?,
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Boolean?,
        useAlarm: Boolean
    ): ApiResult<Unit> = apiResultOf {
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
        Unit
    }

    override suspend fun createPlansSmart(
        regihistoryId: Long,
        startDate: String,
        duration: Int,
        times: List<String>,
        medName: String
    ): ApiResult<Unit> = apiResultOf {
        val body = mapOf(
            "regihistoryId" to regihistoryId,
            "startDate" to startDate,
            "duration" to duration,
            "times" to times,
            "medName" to medName
        )
        api.createPlanSmart(body)
        Unit
    }

    override suspend fun update(userId: Long, plan: Plan): ApiResult<Unit> = apiResultOf {
        api.updatePlan(plan.id, plan.toUpdateRequest())
        syncPlans(userId)
        Unit
    }

    override suspend fun delete(userId: Long, planId: Long): ApiResult<Unit> = apiResultOf {
        api.deletePlan(planId)
        syncPlans(userId)
        Unit
    }

    override suspend fun markAsTaken(planId: Long): ApiResult<Unit> = apiResultOf {
        val response = api.markAsTaken(planId)
        if (!response.isSuccessful) {
            throw Exception("복약 완료 실패: ${response.code()} ${response.message()}")
        }
        Unit
    }

    override suspend fun snoozePlan(planId: Long): ApiResult<Unit> = apiResultOf {
        val response = api.snoozePlan(planId)
        if (!response.isSuccessful) {
            throw Exception("미루기 실패: ${response.code()} ${response.message()}")
        }
        Unit
    }

    override suspend fun getRecentTakenPlans(userId: Long, days: Int): ApiResult<List<Plan>> = apiResultOf {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val endTime = today.atTime(LocalTime.now(zone))
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val startTime = today.minusDays((days - 1).toLong())
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        android.util.Log.d("PlanRepo", "조회 범위: ${java.util.Date(startTime)} ~ ${java.util.Date(endTime)}")

        val entities = dao.getRecentTakenPlans(userId, startTime, endTime)

        android.util.Log.d("PlanRepo", "조회된 데이터: ${entities.size}개")

        entities.map { entity ->
            entity.toDomainLocal()
        }
    }
}