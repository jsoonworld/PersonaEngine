package ai.vip.personaengine.application

import ai.vip.personaengine.api.dto.ChatRequest
import ai.vip.personaengine.api.dto.ChatResponse
import ai.vip.personaengine.api.dto.ThreadWithChatsResponse
import ai.vip.personaengine.domain.chat.Chat
import ai.vip.personaengine.domain.chat.ChatRepository
import ai.vip.personaengine.domain.chat.ChatThread
import ai.vip.personaengine.domain.chat.ChatThreadRepository
import ai.vip.personaengine.domain.user.UserRepository
import ai.vip.personaengine.infrastructure.OpenAiClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChatService(
    private val userRepository: UserRepository,
    private val chatThreadRepository: ChatThreadRepository,
    private val chatRepository: ChatRepository,
    private val openAiClient: OpenAiClient
) {
    @Transactional
    fun createChat(email: String, request: ChatRequest): ChatResponse {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

        val latestThread = chatThreadRepository.findFirstByUserOrderByIdDesc(user)

        val currentThread = if (latestThread == null || isNewThreadRequired(latestThread)) {
            val newThread = ChatThread(user = user)
            chatThreadRepository.save(newThread)
        } else {
            latestThread.lastChatAt = LocalDateTime.now()
            latestThread
        }

        val answer = openAiClient.createAnswer(request.question)

        val chat = Chat(
            thread = currentThread,
            question = request.question,
            answer = answer
        )
        val savedChat = chatRepository.save(chat)

        return ChatResponse.from(savedChat)
    }

    fun listUserChats(email: String, pageable: Pageable): Page<ThreadWithChatsResponse> {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

        val threadsPage: Page<ChatThread> = chatThreadRepository.findByUser(user, pageable)

        return threadsPage.map { thread ->
            val chats = chatRepository.findAllByThread(thread)
            ThreadWithChatsResponse.from(thread, chats)
        }
    }

    private fun isNewThreadRequired(thread: ChatThread): Boolean {
        val minutesSinceLastChat = Duration.between(thread.lastChatAt, LocalDateTime.now()).toMinutes()
        return minutesSinceLastChat >= 30
    }
}
