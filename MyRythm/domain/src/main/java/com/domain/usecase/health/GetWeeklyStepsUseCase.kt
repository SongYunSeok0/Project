package com.domain.usecase.health

import com.domain.model.ApiResult
import com.domain.model.DailyStep
import com.domain.repository.StepRepository
import com.domain.util.apiResultOf
import javax.inject.Inject

class GetWeeklyStepsUseCase @Inject constructor(
    private val stepRepository: StepRepository
) {
    suspend operator fun invoke(): ApiResult<List<DailyStep>> = apiResultOf {
        stepRepository.getWeeklySteps()
    }
}