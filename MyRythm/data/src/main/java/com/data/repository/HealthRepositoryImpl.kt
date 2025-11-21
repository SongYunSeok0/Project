// data/src/main/java/com/data/repository/HealthRepositoryImpl.kt
package com.data.repository

import com.data.mapper.toDomain
import com.data.network.api.HealthApi
import com.domain.model.HeartRateHistory
import com.domain.repository.HealthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HealthRepositoryImpl @Inject constructor(
    private val api: HealthApi,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : HealthRepository {

    override suspend fun getLatestHeartRate(): Int? =
        withContext(io) {
            val res = api.getLatestHeartRate()
            res.bpm // LatestHeartRateResponse.bpm: Int?
        }

    override suspend fun getHeartHistory(): List<HeartRateHistory> =
        withContext(io) {
            api.getHeartHistory()
                .map { it.toDomain() }
        }
}
