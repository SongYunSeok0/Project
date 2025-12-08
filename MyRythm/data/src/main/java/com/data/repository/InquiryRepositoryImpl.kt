package com.data.repository

import com.data.db.dao.InquiryDao
import com.data.db.entity.InquiryEntity
import com.data.mapper.inquiry.toCommentDomain
import com.data.mapper.inquiry.toDomain
import com.data.mapper.toDomain
import com.data.network.api.InquiryApi
import com.domain.model.Inquiry
import com.domain.model.InquiryComment
import com.domain.repository.InquiryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InquiryRepositoryImpl @Inject constructor(
    private val inquiryDao: InquiryDao,
    private val api: InquiryApi,
) : InquiryRepository {

    // 기존 메서드들 (로컬 DB)
    override fun getInquiries(): Flow<List<Inquiry>> =
        inquiryDao.getAllInquiries().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addInquiry(type: String, title: String, content: String) {
        inquiryDao.insertInquiry(
            InquiryEntity(
                type = type,
                title = title,
                content = content
            )
        )
    }

    // 🔥 스태프 전용 메서드들 (서버 API)
    override suspend fun getAllInquiries(): Result<List<Inquiry>> =
        withContext(Dispatchers.IO) {
            runCatching {
                api.getAllInquiries().toDomain()
            }
        }

    override suspend fun getComments(inquiryId: Long): Result<List<InquiryComment>> =
        withContext(Dispatchers.IO) {
            runCatching {
                api.getComments(inquiryId).toCommentDomain()
            }
        }

    override suspend fun addComment(inquiryId: Long, content: String): Result<InquiryComment> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.addComment(
                    inquiryId,
                    mapOf("content" to content)
                )
                if (response.isSuccessful) {
                    response.body()!!.toDomain()
                } else {
                    throw Exception("댓글 작성 실패")
                }
            }
        }
}