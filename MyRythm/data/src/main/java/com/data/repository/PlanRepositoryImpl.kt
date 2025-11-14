package com.data.repository

import com.data.db.dao.PlanDao
import com.data.mapper.toDomain
import com.data.mapper.toEntity
import com.data.network.api.PlanApi
import com.data.network.dto.plan.PlanResponse
import com.data.network.mapper.toCreateRequest
import com.data.network.mapper.toUpdateRequest
import com.data.network.mapper.toDomain as toDomainRemote
import com.domain.model.Plan
import com.domain.repository.PlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepositoryImpl @Inject constructor(
    private val dao: PlanDao,
    private val api: PlanApi
) : PlanRepository {

    override fun observePlans(userId: Long) =
        dao.observePlans(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refresh(userId: Long) = withContext(Dispatchers.IO) {
        val remotePlans: List<PlanResponse> = api.getPlans(userId)
        dao.deleteAllByUser(userId)
        remotePlans.forEach { resp ->
            dao.insert(resp.toDomainRemote().toEntity())
        }
    }

    override suspend fun create(userId: Long, plan: Plan): Long = withContext(Dispatchers.IO) {
        val created = api.createPlan(plan.toCreateRequest(userId))
        val newPlan = plan.copy(id = created.id)
        dao.insert(newPlan.toEntity())
        newPlan.id
    }

    override suspend fun update(userId: Long, plan: Plan) = withContext(Dispatchers.IO) {
        api.updatePlan(plan.id, plan.toUpdateRequest())
        dao.update(plan.toEntity())
    }

    override suspend fun delete(userId: Long, planId: Long) = withContext(Dispatchers.IO) {
        api.deletePlan(planId)
        dao.deleteById(planId)
    }
}
