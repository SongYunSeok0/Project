package com.chatbot.di

import com.domain.usecase.chatbot.AskChatbotUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChatbotEntryPoint {
    fun askChatbotUseCase(): AskChatbotUseCase
}
