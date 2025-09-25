package ai.vip.personaengine.api.dto

import ai.vip.personaengine.domain.chat.Chat
import java.time.LocalDateTime

data class ChatRequest(
    val question: String,
    val isStreaming: Boolean? = false,
    val model: String? = null
)

data class ChatResponse(
    val id: Long,
    val question: String,
    val answer: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(chat: Chat): ChatResponse {
            return ChatResponse(
                id = chat.id,
                question = chat.question,
                answer = chat.answer,
                createdAt = chat.createdAt
            )
        }
    }
}
