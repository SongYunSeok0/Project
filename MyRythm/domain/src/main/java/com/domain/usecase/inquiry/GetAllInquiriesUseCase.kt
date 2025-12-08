package com.domain.usecase.inquiry

import com.domain.model.Inquiry
import com.domain.repository.InquiryRepository
import javax.inject.Inject

class GetAllInquiriesUseCase @Inject constructor(
    private val inquiryRepository: InquiryRepository
) {
    suspend operator fun invoke(): Result<List<Inquiry>> {
        return inquiryRepository.getAllInquiries()
    }
}