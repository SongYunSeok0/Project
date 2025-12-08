package com.domain.usecase.inquiry

import com.domain.model.InquiryComment
import com.domain.repository.InquiryRepository
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val inquiryRepository: InquiryRepository
) {
    suspend operator fun invoke(inquiryId: Long): Result<List<InquiryComment>> {
        return inquiryRepository.getComments(inquiryId)
    }
}