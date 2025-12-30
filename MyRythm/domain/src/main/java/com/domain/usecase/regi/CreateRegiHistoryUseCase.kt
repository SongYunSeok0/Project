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
        return try {
            val id = repository.createRegiHistory(
                regiType = regiType,
                label = label,
                issuedDate = issuedDate,
                useAlarm = useAlarm,
                device = device
            )
            ApiResult.Success(id)
        } catch (e: Exception) {
            ApiResult.Failure(
                DomainError.Unknown(
                    message = e.message ?: "복약 이력 생성 실패"
                )
            )
        }
    }
}
