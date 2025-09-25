package ai.vip.personaengine.domain.chat

import ai.vip.personaengine.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ThreadRepository : JpaRepository<Thread, Long> {
    fun findFirstByUserOrderByIdDesc(user: User): Thread?
}
