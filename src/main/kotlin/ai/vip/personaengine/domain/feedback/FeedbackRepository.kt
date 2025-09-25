package ai.vip.personaengine.domain.feedback

import ai.vip.personaengine.domain.chat.Chat
import ai.vip.personaengine.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun existsByUserAndChat(user: User, chat: Chat): Boolean
}
