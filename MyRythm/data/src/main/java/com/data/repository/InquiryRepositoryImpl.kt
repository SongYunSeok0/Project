package com.data.repository

import com.data.db.dao.InquiryDao
import com.data.db.entity.InquiryEntity
import com.data.mapper.toDomain
import com.data.mapper.toEntity
import com.domain.model.Inquiry
import com.domain.repository.InquiryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InquiryRepositoryImpl @Inject constructor(
    private val inquiryDao: InquiryDao
) : InquiryRepository {

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
}
