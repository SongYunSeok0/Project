// com.domain.repository.ChatbotRepository.kt
package com.domain.repository

import com.domain.model.ChatAnswer

interface ChatbotRepository {
    suspend fun ask(question: String): ChatAnswer
}
