package ai.vip.personaengine.domain.chat

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ChatRepository : JpaRepository<Chat, Long> {
    fun findAllByThread(thread: ChatThread): List<Chat>
    fun countByCreatedAtAfter(dateTime: LocalDateTime): Long
    fun findAllByCreatedAtAfter(dateTime: LocalDateTime): List<Chat>
}
