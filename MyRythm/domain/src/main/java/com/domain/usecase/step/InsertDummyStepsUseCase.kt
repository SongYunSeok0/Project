package com.domain.usecase.step

import com.domain.model.ApiResult
import com.domain.repository.StepRepository
import com.domain.util.apiResultOf
import javax.inject.Inject

class InsertDummyStepsUseCase @Inject constructor(
    private val stepRepository: StepRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> = apiResultOf {
        stepRepository.insertDummyData()
    }
}