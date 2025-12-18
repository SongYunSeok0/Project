package com.data.network.api

import com.data.network.dto.chatbot.ChatRequest
import com.data.network.dto.chatbot.ChatResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatbotApi {

    @POST("rag/drug/")
    suspend fun askDrugRag(
        @Body body: ChatRequest
    ): ChatResponse

    @GET("rag/result/{taskId}/")
    suspend fun getDrugRagResult(@Path("taskId") taskId: String): ChatResponse

}


