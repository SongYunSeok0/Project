package com.domain.model

data class Inquiry(
    val id: Int = 0,
    val type: String,
    val title: String,
    val content: String,
    val answer: String? = null
)
