package com.domain.usecase.chatbot

import javax.inject.Inject

class ExtractMedicationNameUseCase @Inject constructor() {
    operator fun invoke(text: String): String? {
        val pattern = Regex("([가-힣A-Za-z0-9]+(?:정제|정|캡슐|연질캡슐|시럽|액|현탁액|산|콜드|펜))")
        val match = pattern.find(text.replace(" ", ""))
        return match?.groupValues?.getOrNull(1)
    }
}