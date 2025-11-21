// domain/repository/HealthRepository.kt
package com.domain.repository

interface HealthRepository {
    suspend fun getLatestHeartRate(): Int?   // bpm만 쓰면 되니까 Int?로 리턴
}
