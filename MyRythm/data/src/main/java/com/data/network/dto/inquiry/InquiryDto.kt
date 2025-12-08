package com.data.network.dto.inquiry

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InquiryDto(
    @Json(name = "id") val id: Long,
    @Json(name = "user") val user: Long,
    @Json(name = "username") val username: String?,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "category") val category: String,
    @Json(name = "is_answered") val isAnswered: Boolean = false,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?,
    @Json(name = "comment_count") val commentCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class InquiryCommentDto(
    @Json(name = "id") val id: Long,
    @Json(name = "faq") val faq: Long,
    @Json(name = "user") val user: Long,
    @Json(name = "username") val username: String?,
    @Json(name = "content") val content: String,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "is_staff") val isStaff: Boolean = false
)