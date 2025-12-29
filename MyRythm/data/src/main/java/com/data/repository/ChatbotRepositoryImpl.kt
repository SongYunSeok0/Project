package com.data.repository

import android.util.Log
import com.data.network.api.ChatbotApi
import com.data.network.dto.chatbot.ChatRequest
import com.domain.model.ChatAnswer
import com.domain.model.ChatContext
import com.domain.repository.ChatbotRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

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

        Log.d("CHATBOT", "작업 시작: taskId=$taskId")

        // 2) Polling - 최대 300회 시도 (600초 = 10분)
        repeat(300) { attempt ->
            val resultRes = try {
                api.getDrugRagResult(taskId)
            } catch (e: Exception) {
                Log.e("CHATBOT", "폴링 에러 (${attempt + 1}/300): ${e.message}", e)
                delay(2000)
                return@repeat  // 다음 시도로
            }

            Log.d("CHATBOT", "폴링 시도 ${attempt + 1}/300: status=${resultRes.status}")

            when (resultRes.status) {
                "pending", "processing" -> {
                    Log.d("CHATBOT", "⏳ 처리 중...")
                    delay(2000)  // ✅ 상태 확인 후에 대기
                }

                "failed" -> {
                    val errorMsg = resultRes.error ?: "알 수 없는 오류"
                    Log.e("CHATBOT", "❌ RAG 처리 실패: $errorMsg")
                    throw IllegalStateException("RAG 처리 실패: $errorMsg")
                }

                "done" -> {
                    val result = resultRes.result
                        ?: throw IllegalStateException("서버 result 가 비어있습니다.")

                    val contexts: List<ChatContext> =
                        result.contexts?.map { ctx ->
                            ChatContext(
                                chunkId = ctx.chunkId ?: "",
                                itemName = ctx.itemName ?: "",
                                section = ctx.section ?: "",
                                chunkIndex = ctx.chunkIndex ?: 0
                            )
                        } ?: emptyList()

                    Log.d("CHATBOT", "✅ 완료: answer=${result.answer?.take(50)}...")
                    return ChatAnswer(
                        status = "done",
                        question = question,
                        answer = result.answer ?: "(답변 없음)",
                        contexts = contexts
                    )
                }

                else -> {
                    Log.w("CHATBOT", "⚠️ 알 수 없는 상태: ${resultRes.status}")
                    delay(2000)
                }
            }
        }

        Log.e("CHATBOT", "⏱️ RAG 처리 timeout (600초)")
        throw IllegalStateException("RAG 처리 timeout")
    }
}