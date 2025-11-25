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

    override suspend fun refresh(userId: Long) = withContext(Dispatchers.IO) {
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

    override suspend fun update(userId: Long, plan: Plan) {
        api.updatePlan(plan.id, plan.toUpdateRequest())
        refresh(userId)
    }

    override suspend fun delete(userId: Long, planId: Long) {
        api.deletePlan(planId)
        refresh(userId)
    }
}
