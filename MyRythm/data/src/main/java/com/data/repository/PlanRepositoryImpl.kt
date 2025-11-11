package com.data.repository

import com.data.db.dao.PlanDao
import com.data.mapper.toEntities
import com.data.network.api.PlanApi
import com.data.network.dto.plan.PlanResponse
import com.data.network.mapper.toCreateRequest
import com.data.network.mapper.toUpdateRequest
import com.domain.model.Plan
import com.domain.repository.PlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.data.mapper.toDomain as toDomainLocal
import com.data.network.mapper.toDomain as toDomainRemote

@Singleton
class PlanRepositoryImpl @Inject constructor(
    private val dao: PlanDao,
    private val api: PlanApi
) : PlanRepository {

    override fun observePlans(userId: String) =
        dao.observePlans(userId).map { rows -> rows.map { it.toDomainLocal() } }

    override suspend fun refresh(userId: String) = withContext(Dispatchers.IO) {
        val remote: List<PlanResponse> = api.getPlans()
        dao.deleteAllByUser(userId)
        remote.forEach { resp ->
            val plan = resp.toDomainRemote()
            val (entity, meds, times) = plan.toEntities(userId)
            dao.insertAll(entity, meds, times)
        }
    }

    override suspend fun create(userId: String, plan: Plan): Long = withContext(Dispatchers.IO) {
        val newId = api.createPlan(plan.toCreateRequest(userId)).id
        val (entity, meds, times) = plan.copy(id = newId).toEntities(userId)
        dao.insertAll(entity, meds, times)
        newId
    }

    override suspend fun update(userId: String, plan: Plan) {
        withContext(Dispatchers.IO) {
            api.updatePlan(plan.id, plan.toUpdateRequest())
            dao.deletePlan(plan.id)
            val (e, meds, times) = plan.toEntities(userId)
            dao.insertAll(e, meds, times)
        }
    }

    override suspend fun delete(userId: String, planId: Long) {
        withContext(Dispatchers.IO) {
            api.deletePlan(planId)
            dao.deletePlan(planId)
        }
    }
}
