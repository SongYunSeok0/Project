package com.data.repository

import com.data.db.AppRoomDatabase
import com.data.db.entity.PlanEntity
import com.data.db.entity.RegiHistoryEntity
import com.data.network.api.PlanApi
import com.data.network.api.RegiHistoryApi
import com.data.network.dto.plan.PlanCreateRequest
import com.data.network.dto.regihistory.RegiHistoryRequest
import com.data.network.mapper.toDomain
import com.data.network.mapper.toModelList
import com.domain.model.Plan
import com.domain.model.PlanStatus
import com.domain.model.RegiHistory
import com.domain.model.RegiHistoryWithPlans
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
        useAlarm: Boolean,
        device: Long?
    ): Long {
        val req = RegiHistoryRequest(
            regiType = regiType,
            label = label,
            issuedDate = issuedDate,
            useAlarm = useAlarm,
            device = device
        )

        val res = regiHistoryApi.createRegiHistory(req)

        val entity = RegiHistoryEntity(
            id = res.id,
            userId = res.userId,
            regiType = res.regiType,
            label = res.label,
            issuedDate = res.issuedDate,
            useAlarm = res.useAlarm,
            device = res.device
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
                    useAlarm = row.useAlarm,
                    device = row.device
                )
            }
        }

    override suspend fun updateRegiHistory(regi: RegiHistory) {
        val req = RegiHistoryRequest(
            regiType = regi.regiType,
            label = regi.label,
            issuedDate = regi.issuedDate,
            useAlarm = regi.useAlarm,
            device = regi.device
        )

        regiHistoryApi.updateRegiHistory(regi.id, req)

        val entity = RegiHistoryEntity(
            id = regi.id,
            userId = regi.userId,
            regiType = regi.regiType,
            label = regi.label,
            issuedDate = regi.issuedDate,
            useAlarm = regi.useAlarm,
            device = regi.device
        )
        regiHistoryDao.insert(entity)
    }

    override suspend fun deleteRegiHistory(id: Long) {
        regiHistoryApi.deleteRegiHistory(id)
        regiHistoryDao.deleteById(id)
    }

    override suspend fun createPlans(regihistoryId: Long?, list: List<Plan>) {
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
                regihistoryLabel = res.regihistoryLabel,
                medName = res.medName,
                takenAt = res.takenAt,
                exTakenAt = res.exTakenAt,
                mealTime = res.mealTime,
                note = res.note,
                taken = res.taken,
                takenTime = res.takenTime,
                useAlarm = res.useAlarm,
                status = res.status
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
                    regihistoryLabel = row.regihistoryLabel,
                    medName = row.medName,
                    takenAt = row.takenAt,
                    exTakenAt = row.exTakenAt,
                    mealTime = row.mealTime,
                    note = row.note,
                    taken = row.taken,
                    takenTime = row.takenTime,
                    useAlarm = row.useAlarm,
                    status = PlanStatus.from(row.status)
                )
            }
        }

    override fun observePlans(regihistoryId: Long): Flow<List<Plan>> =
        planDao.getByRegiHistory(regihistoryId).map { list ->
            list.map { row ->
                Plan(
                    id = row.id,
                    regihistoryId = row.regihistoryId,
                    regihistoryLabel = row.regihistoryLabel,
                    medName = row.medName,
                    takenAt = row.takenAt,
                    exTakenAt = row.exTakenAt,
                    mealTime = row.mealTime,
                    note = row.note,
                    taken = row.taken,
                    takenTime = row.takenTime,
                    useAlarm = row.useAlarm,
                    status = PlanStatus.from(row.status)
                )
            }
        }

    override suspend fun syncRegiHistories(userId: Long) = withContext(Dispatchers.IO) {
        val remoteRegi = regiHistoryApi.getRegiHistories()
        val remotePlans = planApi.getPlans()

        db.regiHistoryDao().deleteAllByUser(userId = userId)
        db.planDao().deleteAllByUser(userId = userId)

        val regiEntities = remoteRegi.map { res ->
            RegiHistoryEntity(
                id = res.id,
                userId = res.userId,
                regiType = res.regiType,
                label = res.label,
                issuedDate = res.issuedDate,
                useAlarm = res.useAlarm,
                device = res.device
            )
        }
        regiHistoryDao.insertAll(regiEntities)

        val planEntities = remotePlans.map { res ->
            PlanEntity(
                id = res.id,
                regihistoryId = res.regihistoryId,
                regihistoryLabel = res.regihistoryLabel,
                medName = res.medName,
                takenAt = res.takenAt,
                exTakenAt = res.exTakenAt,
                mealTime = res.mealTime,
                note = res.note,
                taken = res.taken,
                takenTime = res.takenTime,
                useAlarm = res.useAlarm,
                status = res.status
            )
        }
        planDao.insertAll(planEntities)
    }

    override suspend fun getUserRegiHistories(userId: Long): Result<List<RegiHistoryWithPlans>> =
        withContext(Dispatchers.IO) {
            runCatching {
                regiHistoryApi.getUserRegiHistories(userId).toModelList()
            }
        }

    override suspend fun getAllRegiHistories(): Result<List<RegiHistoryWithPlans>> =
        withContext(Dispatchers.IO) {
            runCatching {
                regiHistoryApi.getAllRegiHistories().toModelList()
            }
        }

    override suspend fun getUserPlans(userId: Long): Result<List<Plan>> =
        withContext(Dispatchers.IO) {
            runCatching {
                planApi.getUserPlans(userId).map { it.toDomain() }
            }
        }
}