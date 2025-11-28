package com.domain.usecase.health

import com.domain.repository.HeartRateRepository
import com.domain.model.HeartRateHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHeartHistoryUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    // Room Flow 구독
    fun observe(): Flow<List<HeartRateHistory>> {
        return repository.observeHeartHistory()
    }

    // 서버 → Room 동기화
    suspend fun sync() {
        repository.syncHeartHistory()
    }

    // 단발 조회
    suspend operator fun invoke(): List<HeartRateHistory> {
        return repository.getHeartHistory()
    }
}
