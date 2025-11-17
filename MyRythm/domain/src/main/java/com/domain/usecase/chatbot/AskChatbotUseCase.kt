package com.domain.usecase.chatbot

import com.domain.model.ChatAnswer
import com.domain.repository.ChatbotRepository
import javax.inject.Inject

class AskChatbotUseCase @Inject constructor(
    private val repo: ChatbotRepository
) {
    suspend operator fun invoke(question: String): ChatAnswer =
        repo.ask(question)
}
