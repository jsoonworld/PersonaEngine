package ai.vip.personaengine.domain.chat

import ai.vip.personaengine.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatThreadRepository : JpaRepository<ChatThread, Long> {
    fun findFirstByUserOrderByIdDesc(user: User): ChatThread?
    fun findByUser(user: User, pageable: Pageable): Page<ChatThread>
}
