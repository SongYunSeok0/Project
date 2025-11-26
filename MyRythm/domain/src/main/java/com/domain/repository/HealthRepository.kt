// domain/src/main/java/com/domain/repository/HealthRepository.kt
package com.domain.repository

import com.domain.model.HeartRateHistory
import kotlinx.coroutines.flow.Flow

interface HealthRepository {

    suspend fun getLatestHeartRate(): Int?

    suspend fun getHeartHistory(): List<HeartRateHistory>

    fun observeHeartHistory(): Flow<List<HeartRateHistory>>

    suspend fun refreshHeartHistory()
}
