// domain/src/main/java/com/domain/repository/HealthRepository.kt
package com.domain.repository

import com.domain.model.HeartRateHistory

interface HealthRepository {

    suspend fun getLatestHeartRate(): Int?

    suspend fun getHeartHistory(): List<HeartRateHistory>
}
