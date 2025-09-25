package ai.vip.personaengine.domain.feedback

import ai.vip.personaengine.domain.BaseTimeEntity
import ai.vip.personaengine.domain.chat.Chat
import ai.vip.personaengine.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Feedback(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,
    @Column(nullable = false)
    val isPositive: Boolean,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
) : BaseTimeEntity()

enum class FeedbackStatus {
    PENDING, RESOLVED
}
