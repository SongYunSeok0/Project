package com.domain.usecase.regi

import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.repository.RegiRepository
import javax.inject.Inject

class CreateRegiHistoryUseCase @Inject constructor(
    private val repository: RegiRepository
) {
    suspend operator fun invoke(
        regiType: String,
        label: String?,
        issuedDate: String?,
        useAlarm: Boolean,
        device: Long?
    ): ApiResult<Long> {
        if (regiType.isBlank()) {
            return ApiResult.Failure(DomainError.Validation("복약 유형을 선택해주세요"))
        }

        return repository.createRegiHistory(
            regiType = regiType,
            label = label,
            issuedDate = issuedDate,
            useAlarm = useAlarm,
            device = device
        )
    }
}