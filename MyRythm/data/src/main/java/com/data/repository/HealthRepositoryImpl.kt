// data/src/main/java/com/data/repository/HealthRepositoryImpl.kt
package com.data.repository

import com.data.mapper.toDomain
import com.data.network.api.HealthApi
import com.domain.model.HeartRateHistory
import com.domain.repository.HealthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HealthRepositoryImpl @Inject constructor(
    private val api: HealthApi,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : HealthRepository {

    override suspend fun getLatestHeartRate(): Int? =
        withContext(io) {
            val res = api.getLatestHeartRate()
            res.bpm
        }

    override suspend fun getHeartHistory(): List<HeartRateHistory> =
        withContext(io) {
            api.getHeartHistory()
                .map { it.toDomain() }
        }

    // 그래프에서 Flow로 구독하고 싶을 때 사용
    // 지금은 Room 없이 서버에서 한 번 가져와서 내보내는 형태
    override fun observeHeartHistory(): Flow<List<HeartRateHistory>> = flow {
        val list = withContext(io) {
            api.getHeartHistory()
                .map { it.toDomain() }
        }
        emit(list)
    }

    // 나중에 Room 캐시를 붙이면 여기에서 서버 → Room 동기화 수행
    // 현재는 서버를 직접 조회하므로 별도 작업 없음
    override suspend fun refreshHeartHistory() {
        // 필요 없으면 비워둬도 되고,
        // 나중에 캐시 구조 바꾸면 여기 채우면 된다.
    }
}
