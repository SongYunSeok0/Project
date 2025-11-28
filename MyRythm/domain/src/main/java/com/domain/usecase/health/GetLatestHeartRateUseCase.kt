package com.domain.usecase.health

import com.domain.repository.HeartRateRepository
import javax.inject.Inject

class GetLatestHeartRateUseCase @Inject constructor(
    private val repository: HeartRateRepository
) {
    suspend operator fun invoke(): Int? = repository.getLatestHeartRate()
}