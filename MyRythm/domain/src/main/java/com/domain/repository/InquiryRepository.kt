package com.domain.repository

import com.domain.model.Inquiry
import com.domain.model.InquiryComment
import kotlinx.coroutines.flow.Flow

interface InquiryRepository {
    fun getInquiries(): Flow<List<Inquiry>>
    suspend fun addInquiry(type: String, title: String, content: String)

    suspend fun getAllInquiries(): Result<List<Inquiry>>  // 모든 문의사항
    suspend fun getComments(inquiryId: Long): Result<List<InquiryComment>>  // 댓글 목록
    suspend fun addComment(inquiryId: Long, content: String): Result<InquiryComment>  // 댓글 작성
}

