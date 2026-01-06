package com.domain.usecase.health

import com.domain.model.ApiResult
import com.domain.model.HeartRateHistory
import com.domain.repository.HeartRateRepository
import com.domain.util.apiResultOf
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHeartHistoryUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    // Room Flow 구독 - Flow는 ApiResult로 감싸지 않음 (실시간 스트림이므로)
    fun observe(): Flow<List<HeartRateHistory>> {
        return repository.observeHeartHistory()
    }

    // 서버 → Room 동기화
    suspend fun sync(): ApiResult<Unit> = apiResultOf {
        repository.syncHeartHistory()
    }

    // 단발 조회
    suspend operator fun invoke(): ApiResult<List<HeartRateHistory>> = apiResultOf {
        repository.getHeartHistory()
    }
}