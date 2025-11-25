// data/src/main/java/com/data/repository/RegiRepositoryImpl.kt
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

    // ---------------- RegiHistory ----------------

    override suspend fun createRegiHistory(
        regiType: String,
        label: String?,
        issuedDate: String?
    ): Long {
        // domain → DTO
        val req = RegiHistoryRequest(
            regiType = regiType,
            label = label,
            issuedDate = issuedDate
        )

        val res = regiHistoryApi.createRegiHistory(req)

        // 응답 → Room Entity
        val entity = RegiHistoryEntity(
            id = res.id,
            userId = res.userId,
            regiType = res.regiType,
            label = res.label,
            issuedDate = res.issuedDate
        )
        regiHistoryDao.insert(entity)

        return res.id
    }

    override fun observeRegiHistories(): Flow<List<RegiHistory>> =
        regiHistoryDao.getAll().map { list ->
            list.map { row ->
                RegiHistory(
                    id = row.id,
                    userId = row.userId,
                    regiType = row.regiType,
                    label = row.label,
                    issuedDate = row.issuedDate
                )
            }
        }

    // ---------------- Plans ----------------

    override suspend fun createPlans(
        regiHistoryId: Long,
        list: List<Plan>
    ) {
        val entities = mutableListOf<PlanEntity>()

        for (plan in list) {
            // domain → DTO
            val req = PlanCreateRequest(
                regiHistoryId = regiHistoryId,
                medName = plan.medName,
                takenAt = plan.takenAt,
                mealTime = plan.mealTime,
                note = plan.note,
                taken = plan.taken
            )

            val res = planApi.createPlan(req)

            // 응답 → Room Entity
            entities += PlanEntity(
                id = res.id,
                regiHistoryId = res.regiHistoryId,
                medName = res.medName,
                takenAt = res.takenAt,
                mealTime = res.mealTime,
                note = res.note,
                taken = res.taken
            )
        }

        planDao.insertAll(entities)
    }

    override fun observeAllPlans(userId: Long): Flow<List<Plan>> =
        planDao.getAllByUser(userId).map { list ->
            list.map { row ->
                Plan(
                    id = row.id,
                    regiHistoryId = row.regiHistoryId,
                    medName = row.medName,
                    takenAt = row.takenAt,
                    mealTime = row.mealTime,
                    note = row.note,
                    taken = row.taken
                )
            }
        }

    override fun observePlans(regiHistoryId: Long): Flow<List<Plan>> =
        planDao.getByRegiHistory(regiHistoryId).map { list ->
            list.map { row ->
                Plan(
                    id = row.id,
                    regiHistoryId = row.regiHistoryId,
                    medName = row.medName,
                    takenAt = row.takenAt,
                    mealTime = row.mealTime,
                    note = row.note,
                    taken = row.taken
                )
            }
        }
}
