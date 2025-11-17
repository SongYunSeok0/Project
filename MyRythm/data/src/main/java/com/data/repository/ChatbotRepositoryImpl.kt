// com.data.repository.ChatbotRepositoryImpl.kt
package com.data.repository

import com.data.network.api.ChatbotApi
import com.data.network.dto.chatbot.ChatRequest
import com.domain.model.ChatAnswer
import com.domain.repository.ChatbotRepository
import javax.inject.Inject

class ChatbotRepositoryImpl @Inject constructor(
    private val api: ChatbotApi
) : ChatbotRepository {

    override suspend fun ask(question: String): ChatAnswer {
        val res = api.askDrugRag(ChatRequest(question = question))
        return ChatAnswer(
            question = res.question,
            answer = res.answer
        )
    }
}
