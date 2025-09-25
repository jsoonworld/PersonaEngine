package ai.vip.personaengine.api.dto

import ai.vip.personaengine.domain.chat.Chat
import ai.vip.personaengine.domain.chat.ChatThread
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
    val createdAt: LocalDateTime?
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

data class ThreadWithChatsResponse(
    val threadId: Long,
    val userId: Long,
    val lastChatAt: LocalDateTime,
    val createdAt: LocalDateTime?,
    val chats: List<ChatResponse>
) {
    companion object {
        fun from(thread: ChatThread, chats: List<Chat>): ThreadWithChatsResponse {
            return ThreadWithChatsResponse(
                threadId = thread.id,
                userId = thread.user.id,
                lastChatAt = thread.lastChatAt,
                createdAt = thread.createdAt,
                chats = chats.map { ChatResponse.from(it) }
            )
        }
    }
}
