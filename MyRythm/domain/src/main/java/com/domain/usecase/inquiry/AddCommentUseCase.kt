package com.domain.usecase.inquiry

import com.domain.model.InquiryComment
import com.domain.repository.InquiryRepository
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val inquiryRepository: InquiryRepository
) {
    suspend operator fun invoke(inquiryId: Long, content: String): Result<InquiryComment> {
        return inquiryRepository.addComment(inquiryId, content)
    }
}