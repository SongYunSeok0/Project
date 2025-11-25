package com.data.repository

import com.data.db.dao.PlanDao
import com.data.mapper.toEntity
import com.data.mapper.toDomainLocal
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

    // ----------------------------------------------------
    // 🔥 1. 로컬 Plan 관찰 (userId 기반)
    // ----------------------------------------------------
    override fun observePlans(userId: Long): Flow<List<Plan>> =
        dao.getAllByUser(userId).map { list ->
            list.map { it.toDomainLocal() }
        }

    // ----------------------------------------------------
    // 🔥 2. 서버 → 로컬 동기화
    // ----------------------------------------------------
    override suspend fun refresh(userId: Long) = withContext(Dispatchers.IO) {
        val remotePlans = api.getPlans()          // 서버 목록
        val domainPlans = remotePlans.map { it.toDomain() }
        val entities = domainPlans.map { it.toEntity() }

        // 기존 삭제 (특정 userId만 삭제)
        dao.deleteAllByUser(userId)

        // 새로 저장
        dao.insertAll(entities)
    }

    // ----------------------------------------------------
    // 🔥 3. 생성 (서버에만 POST)
    // ----------------------------------------------------
    override suspend fun create(
        prescriptionId: Long?,   // ❗ 실제로는 regiHistoryId 이게 맞음
        medName: String,
        takenAt: Long,
        mealTime: String?,
        note: String?,
        taken: Long?
    ) {
        val body = PlanCreateRequest(
            regiHistoryId = prescriptionId,   // 서버가 이 이름 사용
            medName = medName,
            takenAt = takenAt,
            mealTime = mealTime,
            note = note,
            taken = taken
        )
        api.createPlan(body)
    }

    // ----------------------------------------------------
    // 🔥 4. 수정
    //   - 서버 PATCH
    //   - 로컬 업데이트 대신 refresh() 로 전체 동기화 추천
    // ----------------------------------------------------
    override suspend fun update(userId: Long, plan: Plan) {
        api.updatePlan(plan.id, plan.toUpdateRequest())
        // 로컬 동기화
        refresh(userId)
    }

    // ----------------------------------------------------
    // 🔥 5. 삭제
    // ----------------------------------------------------
    override suspend fun delete(userId: Long, planId: Long) {
        api.deletePlan(planId)
        // 로컬 동기화
        refresh(userId)
    }
}
