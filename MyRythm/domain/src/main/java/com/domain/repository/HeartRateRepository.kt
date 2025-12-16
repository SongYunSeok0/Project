package com.domain.repository

import com.domain.model.HeartRateHistory
import kotlinx.coroutines.flow.Flow

interface HeartRateRepository {

    suspend fun getLatestHeartRate(): Int?

    suspend fun getHeartHistory(): List<HeartRateHistory>

    fun observeHeartHistory(): Flow<List<HeartRateHistory>>

    suspend fun syncHeartHistory()

    suspend fun getWeeklyHeartRates(): List<HeartRateHistory>
}
