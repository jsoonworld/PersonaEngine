package ai.vip.personaengine.domain.feedback

import ai.vip.personaengine.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun existsByUserAndChat(user: User, chat: ai.vip.personaengine.domain.chat.Chat): Boolean
    fun findByUserAndIsPositive(user: User, isPositive: Boolean, pageable: Pageable): Page<Feedback>
    fun findByUser(user: User, pageable: Pageable): Page<Feedback>
    fun findByIsPositive(isPositive: Boolean, pageable: Pageable): Page<Feedback>
}
