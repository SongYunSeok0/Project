package com.domain.usecase.health

import com.domain.repository.HealthRepository
import com.domain.model.HeartRateHistory

import javax.inject.Inject

class GetHeartHistoryUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    suspend operator fun invoke(): List<HeartRateHistory> {
        // 예외는 ViewModel 쪽 runCatching에서 처리
        return repository.getHeartHistory()
    }
}