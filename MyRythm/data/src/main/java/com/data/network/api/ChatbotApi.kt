package com.data.network.api

import com.data.network.dto.chatbot.ChatRequest
import com.data.network.dto.chatbot.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatbotApi {

    @POST("rag/drug/")
    suspend fun askDrugRag(
        @Body body: ChatRequest
    ): ChatResponse
}
