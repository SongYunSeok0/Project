package com.data.repository

import android.util.Log
import com.data.network.api.ChatbotApi
import com.data.network.dto.chatbot.ChatRequest
import com.domain.model.ChatAnswer
import com.domain.model.ChatContext
import com.domain.repository.ChatbotRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.collections.map

class ChatbotRepositoryImpl @Inject constructor(
    private val api: ChatbotApi
) : ChatbotRepository {

    override suspend fun ask(question: String): ChatAnswer {

        // 1) async 요청
        val initRes = api.askDrugRag(
            ChatRequest(question = question, mode = "async")
        )

        val taskId = initRes.taskId
            ?: throw IllegalStateException("task_id 가 응답에 없습니다.")

        // 2) Polling
        repeat(300) {
            delay(2000)

            val resultRes = api.getDrugRagResult(taskId)

            when (resultRes.status) {
                "pending", "processing" -> { /* 계속 대기 */
                }

                "failed" -> {
                    throw IllegalStateException("RAG 처리 실패: ${resultRes.error}")
                }

                "done" -> {
                    val result = resultRes.result
                        ?: throw IllegalStateException("서버 result 가 비어있습니다.")

                    // 💡 null-safe 매핑 (에러 17개 나던 구간 해결)
                    val contexts: List<ChatContext> =
                        result.contexts?.map { ctx ->
                            ChatContext(
                                chunkId = ctx.chunkId ?: "",
                                itemName = ctx.itemName ?: "",
                                section = ctx.section ?: "",
                                chunkIndex = ctx.chunkIndex ?: 0
                            )
                        } ?: emptyList()

                    Log.d("CHATBOT", "result = ${resultRes}")
                    return ChatAnswer(
                        status = "done",
                        question = question,
                        answer = result.answer ?: "(답변 없음)",
                        contexts = contexts
                    )
                }
            }
        }

        throw IllegalStateException("RAG 처리 timeout")
    }
}
