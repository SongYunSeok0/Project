package com.domain.usecase.step

import com.domain.repository.StepRepository
import javax.inject.Inject

class InsertDummyStepsUseCase @Inject constructor(
    private val stepRepository: StepRepository
) {
    suspend operator fun invoke() {
        stepRepository.insertDummyData()
    }
}