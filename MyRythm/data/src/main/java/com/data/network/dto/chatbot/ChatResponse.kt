package com.data.network.dto.chatbot

import com.squareup.moshi.Json

data class ChatResponse(
    @Json(name = "status") val status: String,
    @Json(name = "task_id") val taskId: String? = null,
    @Json(name = "question") val question: String? = null,
    @Json(name = "result") val result: ChatResultDto? = null,
    @Json(name = "error") val error: String? = null
)

data class ChatResultDto(
    @Json(name = "answer")
    val answer: String? = null,

    @Json(name = "contexts")
    val contexts: List<ChatContextDto>? = emptyList()
)
