package com.domain.usecase.health

import com.domain.model.DailyStep
import com.domain.repository.StepRepository
import javax.inject.Inject

class GetWeeklyStepsUseCase @Inject constructor(
    private val stepRepository: StepRepository
) {
    suspend operator fun invoke(): List<DailyStep> {
        return stepRepository.getWeeklySteps()
    }
}