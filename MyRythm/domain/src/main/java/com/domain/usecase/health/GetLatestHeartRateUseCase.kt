package com.domain.usecase.health

import com.domain.model.ApiResult
import com.domain.repository.HeartRateRepository
import com.domain.util.apiResultOf
import javax.inject.Inject

class GetLatestHeartRateUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke(): ApiResult<Int?> = apiResultOf {
        repository.getLatestHeartRate()
    }
}