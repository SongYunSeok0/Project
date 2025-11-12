package com.data.mapper

import com.data.db.entity.InquiryEntity
import com.domain.model.Inquiry

fun InquiryEntity.toDomain(): Inquiry = Inquiry(
    id = id,
    type = type,
    title = title,
    content = content,
    answer = answer
)

fun Inquiry.toEntity(): InquiryEntity = InquiryEntity(
    id = id,
    type = type,
    title = title,
    content = content,
    answer = answer
)
