package com.domain.usecase.mypage

import com.domain.repository.MediRecordRepository
import javax.inject.Inject

class GetMediRecordsUseCase @Inject constructor(
    private val repo: MediRecordRepository
) {
    operator fun invoke() = repo.getRecords()
}
