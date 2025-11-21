package com.domain.usecase

import com.domain.repository.HealthRepository
import javax.inject.Inject

class GetLatestHeartRateUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    suspend operator fun invoke(): Int? = repository.getLatestHeartRate()
}
