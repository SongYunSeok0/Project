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
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RegiRepositoryImpl @Inject constructor(
    private val regiHistoryApi: RegiHistoryApi,
    private val planApi: PlanApi,
    private val db: AppRoomDatabase
) : RegiRepository {

    private val regiHistoryDao = db.regiHistoryDao()
    private val planDao = db.planDao()

    // ---------------- Create ----------------

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

    // ---------------- Read ----------------

    override fun observeRegiHistories(): Flow<List<RegiHistory>> =
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

    // ---------------- Update ----------------

    override suspend fun updateRegiHistory(regi: RegiHistory) {
        val req = RegiHistoryRequest(
            regiType = regi.regiType,
            label = regi.label,
            issuedDate = regi.issuedDate,
            useAlarm = regi.useAlarm
        )

        // 서버 PATCH
        regiHistoryApi.updateRegiHistory(regi.id, req)

        // 로컬 업데이트
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

    // ---------------- Delete ----------------

    override suspend fun deleteRegiHistory(id: Long) {
        // 서버 DELETE
        regiHistoryApi.deleteRegiHistory(id)

        // 로컬 삭제
        // Room: CASCADE 걸려 있으면 Plan 도 같이 삭제됨
        regiHistoryDao.deleteById(id)
    }

    // ---------------- Plans ----------------

    override suspend fun createPlans(
        regihistoryId: Long,
        list: List<Plan>
    ) {
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
                    mealTime = row.mealTime,
                    note = row.note,
                    taken = row.taken,
                    useAlarm = row.useAlarm
                )
            }
        }
}
