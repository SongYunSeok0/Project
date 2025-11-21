package com.data.repository

import com.data.network.api.HealthApi
import com.domain.repository.HealthRepository
import javax.inject.Inject

class HealthRepositoryImpl @Inject constructor(
    private val api: HealthApi
) : HealthRepository {

    override suspend fun getLatestHeartRate(): Int? {
        val res = api.getLatestHeartRate()
        return res.bpm   // null이면 그대로 null 리턴
    }
}
