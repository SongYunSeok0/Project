package com.domain.repository

import com.domain.model.Inquiry
import kotlinx.coroutines.flow.Flow

interface InquiryRepository {
    fun getInquiries(): Flow<List<Inquiry>>
    suspend fun addInquiry(type: String, title: String, content: String)
}

