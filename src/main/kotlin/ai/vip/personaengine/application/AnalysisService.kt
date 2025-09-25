package ai.vip.personaengine.application

import ai.vip.personaengine.api.dto.UserActivityResponse
import ai.vip.personaengine.domain.chat.ChatRepository
import ai.vip.personaengine.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AnalysisService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) {
    fun getUserActivity(): UserActivityResponse {
        val yesterday = LocalDateTime.now().minusDays(1)

        val signUpCount = userRepository.countByCreatedAtAfter(yesterday)
        val chatCount = chatRepository.countByCreatedAtAfter(yesterday)
        val loginCount = 0L

        return UserActivityResponse(signUpCount, loginCount, chatCount)
    }

    fun generateChatReport(): String {
        val yesterday = LocalDateTime.now().minusDays(1)
        val chats = chatRepository.findAllByCreatedAtAfter(yesterday)

        val csvBuilder = StringBuilder()
        csvBuilder.append("Chat ID,User Email,Question,Answer,Created At\n")
        chats.forEach { chat ->
            csvBuilder.append("${chat.id},${chat.thread.user.email},\"${chat.question}\",\"${chat.answer}\",${chat.createdAt}\n")
        }
        return csvBuilder.toString()
    }
}
