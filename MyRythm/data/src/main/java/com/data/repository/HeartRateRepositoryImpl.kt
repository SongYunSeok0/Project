package com.data.repository

import android.util.Log
import com.data.db.AppRoomDatabase
import com.data.mapper.toDomain
import com.data.mapper.toEntity
import com.data.network.api.HeartRateApi
import com.domain.model.HeartRateHistory
import com.domain.repository.HeartRateRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HeartRateRepositoryImpl @Inject constructor(
    private val api: HeartRateApi,
    private val db: AppRoomDatabase,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : HeartRateRepository {

    private val dao = db.heartRateDao()

    override suspend fun getLatestHeartRate(): Int? =
        withContext(io) {
            val res = api.getLatestHeartRate()
            res.bpm
        }

    override suspend fun getHeartHistory(): List<HeartRateHistory> =
        withContext(io) {
            dao.getAll()                 // Flow<List<HeartRateEntity>>
                .map { list -> list.map { it.toDomain() } }
                .first()                 // 한번 값 뽑기
        }

    // UI에서 항상 Flow로 구독
    override fun observeHeartHistory(): Flow<List<HeartRateHistory>> =
        dao.getAll()
            .map { list -> list.map { it.toDomain() } }

    // 서버 → Room 동기화 (핵심)
    override suspend fun syncHeartHistory() {
        withContext(io) {
            val remote = api.getHeartHistory()
            dao.clear()
            dao.insertAll(remote.map { it.toEntity() })
        }
    }

    override suspend fun getWeeklyHeartRates(): List<HeartRateHistory> {
        val sevenDaysAgo = java.time.LocalDate.now().minusDays(7).toString() // 7 -> 6

        return dao.getLastWeek(sevenDaysAgo).map { entity ->
            HeartRateHistory(
                bpm = entity.bpm,
                collectedAt = entity.collectedAt
            )
        }
    }
}
