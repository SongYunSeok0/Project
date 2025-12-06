package com.data.repository

import com.data.network.api.ChatbotApi
import com.data.network.dto.chatbot.ChatRequest
import com.domain.model.ChatAnswer
import com.domain.model.ChatContext
import com.domain.repository.ChatbotRepository
import javax.inject.Inject

class ChatbotRepositoryImpl @Inject constructor(
    private val api: ChatbotApi
) : ChatbotRepository {

    override suspend fun ask(question: String): ChatAnswer {
        val res = api.askDrugRag(ChatRequest(question = question, mode = "sync"))

        return ChatAnswer(
            status = res.status,
            question = res.question,
            answer = res.result.answer,
            contexts = res.result.contexts.map { ctx ->
                ChatContext(
                    chunkId = ctx.chunkId,
                    itemName = ctx.itemName,
                    section = ctx.section,
                    chunkIndex = ctx.chunkIndex
                )
            }
        )
    }
}
