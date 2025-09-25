package ai.vip.personaengine.api.dto

import ai.vip.personaengine.domain.feedback.Feedback
import ai.vip.personaengine.domain.feedback.FeedbackStatus
import java.time.LocalDateTime

data class FeedbackRequest(
    val chatId: Long,
    val isPositive: Boolean
)

data class FeedbackResponse(
    val feedbackId: Long,
    val userId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun from(feedback: Feedback): FeedbackResponse {
            return FeedbackResponse(
                feedbackId = feedback.id,
                userId = feedback.user.id,
                chatId = feedback.chat.id,
                isPositive = feedback.isPositive,
                status = feedback.status,
                createdAt = feedback.createdAt
            )
        }
    }
}
