package com.domain.usecase.inquiry

import com.domain.repository.InquiryRepository
import javax.inject.Inject

class AddInquiryUseCase @Inject constructor(
    private val repository: InquiryRepository
) {
    suspend operator fun invoke(type: String, title: String, content: String) {
        return repository.addInquiry(type, title, content)
    }
}