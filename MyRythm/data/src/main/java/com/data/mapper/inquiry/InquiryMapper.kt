package com.data.mapper.inquiry

import com.data.db.entity.InquiryCommentEntity
import com.data.db.entity.InquiryEntity
import com.data.network.dto.inquiry.InquiryCommentDto
import com.data.network.dto.inquiry.InquiryDto
import com.domain.model.Inquiry
import com.domain.model.InquiryComment

// DTO → Domain
fun InquiryDto.toDomain(): Inquiry {
    return Inquiry(
        id = id,
        userId = user,
        username = username,
        type = category,
        title = title,
        content = content,
        isAnswered = isAnswered,
        createdAt = createdAt,
        updatedAt = updatedAt,
        commentCount = commentCount
    )
}

fun InquiryCommentDto.toDomain(): InquiryComment {
    return InquiryComment(
        id = id,
        inquiryId = faq,
        userId = user,
        username = username,
        content = content,
        createdAt = createdAt,
        isStaff = isStaff
    )
}

// DTO → Entity
fun InquiryDto.toEntity(): InquiryEntity {
    return InquiryEntity(
        id = id,
        userId = user,
        username = username,
        type = category,
        title = title,
        content = content,
        answer = null,  // Entity에는 남아있음 (하위호환)
        isAnswered = isAnswered,
        createdAt = createdAt,
        updatedAt = updatedAt,
        commentCount = commentCount
    )
}

fun InquiryCommentDto.toEntity(): InquiryCommentEntity {
    return InquiryCommentEntity(
        id = id,
        inquiryId = faq,
        userId = user,
        username = username,
        content = content,
        createdAt = createdAt,
        isStaff = isStaff
    )
}

// Entity → Domain
fun InquiryEntity.toDomain(): Inquiry {
    return Inquiry(
        id = id,
        userId = userId,
        username = username,
        type = type,
        title = title,
        content = content,
        isAnswered = isAnswered,
        createdAt = createdAt,
        updatedAt = updatedAt,  // 🔥 추가
        commentCount = commentCount
        // answer 필드 제거 (Entity에는 있지만 Domain에는 없음)
    )
}

fun InquiryCommentEntity.toDomain(): InquiryComment {
    return InquiryComment(
        id = id,
        inquiryId = inquiryId,
        userId = userId,
        username = username,
        content = content,
        createdAt = createdAt,
        isStaff = isStaff
    )
}

// List 변환
fun List<InquiryDto>.toDomain(): List<Inquiry> = map { it.toDomain() }
fun List<InquiryCommentDto>.toCommentDomain(): List<InquiryComment> = map { it.toDomain() }
fun List<InquiryEntity>.toInquiryDomain(): List<Inquiry> = map { it.toDomain() }
fun List<InquiryCommentEntity>.toCommentEntityDomain(): List<InquiryComment> = map { it.toDomain() }