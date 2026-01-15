package com.domain.usecase.chatbot

import com.domain.model.ChatQueryContext
import javax.inject.Inject

class BuildChatContextUseCase @Inject constructor(
    private val extractMedicationName: ExtractMedicationNameUseCase
) {
    operator fun invoke(
        currentInput: String,
        lastMedicationName: String?
    ): ChatQueryContext {  // 반환 타입 변경
        val extractedMed = extractMedicationName(currentInput)

        return ChatQueryContext(  // 생성자 이름 변경
            originalQuery = currentInput,
            effectiveQuery = when {
                extractedMed != null -> currentInput
                lastMedicationName != null -> "$lastMedicationName $currentInput"
                else -> currentInput
            },
            medicationName = extractedMed ?: lastMedicationName
        )
    }
}