// com.data.network.dto.chatbot.ChatContextDto.kt
package com.data.network.dto.chatbot

import com.squareup.moshi.Json

data class ChatContextDto(
    @Json(name = "chunk_id") val chunkId: String,
    @Json(name = "item_name") val itemName: String,
    val section: String,
    @Json(name = "chunk_index") val chunkIndex: Int
)