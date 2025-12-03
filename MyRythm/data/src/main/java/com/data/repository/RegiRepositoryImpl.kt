package com.data.repository

import com.data.db.AppRoomDatabase
import com.data.db.entity.PlanEntity
import com.data.db.entity.RegiHistoryEntity
import com.data.network.api.PlanApi
import com.data.network.api.RegiHistoryApi
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.dto.regihistory.RegiHistoryRequest
import com.domain.model.Plan
import com.domain.model.RegiHistory
import com.domain.repository.RegiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RegiRepositoryImpl @Inject constructor(
    private val regiHistoryApi: RegiHistoryApi,
    private val planApi: PlanApi,
    private val db: AppRoomDatabase
) : RegiRepository {

    private val regiHistoryDao = db.regiHistoryDao()
    private val planDao = db.planDao()

    override suspend fun createRegiHistory(
        regiType: String,
        label: String?,
        issuedDate: String?,
        useAlarm: Boolean
    ): Long {
        val req = RegiHistoryRequest(
            regiType = regiType,
            label = label,
            issuedDate = issuedDate,
            useAlarm = useAlarm
        )

        val res = regiHistoryApi.createRegiHistory(req)

        val entity = RegiHistoryEntity(
            id = res.id,
            userId = res.userId,
            regiType = res.regiType,
            label = res.label,
            issuedDate = res.issuedDate,
            useAlarm = res.useAlarm
        )
        regiHistoryDao.insert(entity)

        return res.id
    }

    override fun getRegiHistories(): Flow<List<RegiHistory>> =
        regiHistoryDao.getAll().map { list ->
            list.map { row ->
                RegiHistory(
                    id = row.id,
                    userId = row.userId,
                    regiType = row.regiType,
                    label = row.label,
                    issuedDate = row.issuedDate,
                    useAlarm = row.useAlarm
                )
            }
        }

    override suspend fun updateRegiHistory(regi: RegiHistory) {
        val req = RegiHistoryRequest(
            regiType = regi.regiType,
            label = regi.label,
            issuedDate = regi.issuedDate,
            useAlarm = regi.useAlarm
        )

        regiHistoryApi.updateRegiHistory(regi.id, req)

        val entity = RegiHistoryEntity(
            id = regi.id,
            userId = regi.userId,
            regiType = regi.regiType,
            label = regi.label,
            issuedDate = regi.issuedDate,
            useAlarm = regi.useAlarm
        )
        regiHistoryDao.insert(entity)
    }

    override suspend fun deleteRegiHistory(id: Long) {
        regiHistoryApi.deleteRegiHistory(id)
        regiHistoryDao.deleteById(id)
    }

    override suspend fun createPlans(regihistoryId: Long, list: List<Plan>) {
        val entities = mutableListOf<PlanEntity>()

        for (plan in list) {
            val req = PlanCreateRequest(
                regihistoryId = regihistoryId,
                medName = plan.medName,
                takenAt = plan.takenAt,
                mealTime = plan.mealTime,
                note = plan.note,
                taken = plan.taken,
                useAlarm = plan.useAlarm
            )

            val res = planApi.createPlan(req)

            entities += PlanEntity(
                id = res.id,
                regihistoryId = res.regihistoryId,
                medName = res.medName,
                takenAt = res.takenAt,
                exTakenAt = res.exTakenAt,  // 👈 추가
                mealTime = res.mealTime,
                note = res.note,
                taken = res.taken,
                useAlarm = res.useAlarm
            )
        }

        planDao.insertAll(entities)
    }

    override fun observeAllPlans(userId: Long): Flow<List<Plan>> =
        planDao.getAllByUser(userId).map { list ->
            list.map { row ->
                Plan(
                    id = row.id,
                    regihistoryId = row.regihistoryId,
                    medName = row.medName,
                    takenAt = row.takenAt,
                    exTakenAt = row.exTakenAt,  // 👈 추가
                    mealTime = row.mealTime,
                    note = row.note,
                    taken = row.taken,
                    useAlarm = row.useAlarm
                )
            }
        }

    override fun observePlans(regihistoryId: Long): Flow<List<Plan>> =
        planDao.getByRegiHistory(regihistoryId).map { list ->
            list.map { row ->
                Plan(
                    id = row.id,
                    regihistoryId = row.regihistoryId,
                    medName = row.medName,
                    takenAt = row.takenAt,
                    exTakenAt = row.exTakenAt,  // 👈 추가
                    mealTime = row.mealTime,
                    note = row.note,
                    taken = row.taken,
                    useAlarm = row.useAlarm
                )
            }
        }

    /** 🔥 새로 추가된 sync(userId) 구현 */
    override suspend fun syncRegiHistories(userId: Long) = withContext(Dispatchers.IO) {

        // 1. 서버에서 최신 데이터 가져오기
        val remoteRegi = regiHistoryApi.getRegiHistories()   // List<RegiHistoryResponse>
        val remotePlans = planApi.getPlans()                 // List<PlanResponse>

        // 2. 기존 userId 데이터 삭제
        //    regihistory와 plan 모두 삭제됨
        db.regiHistoryDao().deleteAllByUser(userId = userId)
        db.planDao().deleteAllByUser(userId = userId)

        // 3. 최신 RegiHistory 저장
        val regiEntities = remoteRegi.map { res ->
            RegiHistoryEntity(
                id = res.id,
                userId = res.userId,
                regiType = res.regiType,
                label = res.label,
                issuedDate = res.issuedDate,
                useAlarm = res.useAlarm
            )
        }

        regiHistoryDao.insertAll(regiEntities)

        // 4. 최신 Plan 저장
        val planEntities = remotePlans.map { res ->
            PlanEntity(
                id = res.id,
                regihistoryId = res.regihistoryId,
                medName = res.medName,
                takenAt = res.takenAt,
                exTakenAt = res.exTakenAt,  // 👈 추가
                mealTime = res.mealTime,
                note = res.note,
                taken = res.taken,
                useAlarm = res.useAlarm
            )
        }

        planDao.insertAll(planEntities)
    }
}