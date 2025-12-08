package com.domain.model

data class Inquiry(
    val id: Long = 0,
    val userId: Long = 0,
    val username: String? = null,
    val type: String,  // category
    val title: String,
    val content: String,
    val isAnswered: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val commentCount: Int = 0,
    val comments: List<InquiryComment> = emptyList()
)

data class InquiryComment(
    val id: Long = 0,
    val inquiryId: Long = 0,
    val userId: Long = 0,
    val username: String? = null,
    val content: String,
    val createdAt: String? = null,
    val isStaff: Boolean = false
)