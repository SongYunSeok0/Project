package com.data.repository

import com.data.db.dao.PlanDao
import com.data.mapper.toDomain              // DB → Domain
import com.data.mapper.toDomainLocal
import com.data.mapper.toEntity             // Domain → Entity
import com.data.network.api.PlanApi
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.mapper.toDomain
import com.data.network.mapper.toUpdateRequest
import com.data.network.mapper.toDomain as toRemoteDomain  // Remote → Domain (alias)
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

    // ----------------------------
    // 🔥 로컬 DB → 도메인
    // ----------------------------
    override fun observePlans(userId: Long): Flow<List<Plan>> =
        dao.observePlans(userId).map { list ->
            list.map { it.toDomainLocal() }
        }

    // ----------------------------
    // 🔥 서버 → 로컬 동기화
    // ----------------------------
    override suspend fun refresh(userId: Long) = withContext(Dispatchers.IO) {
        val remote = api.getPlans()
        dao.deleteAllByUser(userId)
        remote.forEach { resp ->
            dao.insert(resp.toDomain().toEntity())
        }
    }


    // ----------------------------
    // 🔥 서버로 새로운 Plan 생성
    // ----------------------------
    override suspend fun create(
        prescriptionId: Long?,
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Long?
    ) {
        val body = PlanCreateRequest(
            prescriptionId = prescriptionId,
            medName = medName,
            takenAt = takenAt,
            mealTime = mealTime,
            note = note,
            taken = taken
        )
        api.createPlan(body)
    }

    // ----------------------------
    // 🔥 수정
    // ----------------------------
    override suspend fun update(userId: Long, plan: Plan) {
        api.updatePlan(plan.id, plan.toUpdateRequest())
        dao.update(plan.toEntity())
    }

    // ----------------------------
    // 🔥 삭제
    // ----------------------------
    override suspend fun delete(userId: Long, planId: Long) {
        api.deletePlan(planId)
        dao.deleteById(planId)
    }
}
