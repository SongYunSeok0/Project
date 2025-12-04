package com.domain.usecase.inquiry

import com.domain.model.Inquiry
import com.domain.repository.InquiryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInquiriesUseCase @Inject constructor(
    private val repository: InquiryRepository
) {
    operator fun invoke(): Flow<List<Inquiry>> {
        return repository.getInquiries()
    }
}