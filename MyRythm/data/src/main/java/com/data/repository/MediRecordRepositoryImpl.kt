package com.data.repository

import com.data.db.dao.PlanDao
import com.data.db.dao.RegiHistoryDao
import com.data.db.entity.PlanWithRegi
import com.data.mapper.toDomain
import com.domain.model.MediRecord
import com.domain.repository.MediRecordRepository
import com.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class MediRecordRepositoryImpl @Inject constructor(
    private val planDao: PlanDao,
    private val regiDao: RegiHistoryDao,
    private val authRepository: AuthRepository
) : MediRecordRepository {

    override fun getRecords(): Flow<List<MediRecord>> {

        val userId = authRepository.getUserId()

        val plansFlow = planDao.getAllByUser(userId)
        val regisFlow = regiDao.getAll()

        return combine(plansFlow, regisFlow) { plans, regis ->
            val map = regis.associateBy { it.id }
            plans.map { plan ->
                PlanWithRegi(plan, map[plan.regihistoryId]).toDomain()
            }
        }
    }
}
