package com.domain.usecase.regi

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
    ): Long {
        return repository.createRegiHistory(
            regiType = regiType,
            label = label,
            issuedDate = issuedDate,
            useAlarm = useAlarm,
            device = device
        )
    }
}
