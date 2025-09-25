package ai.vip.personaengine.application

import ai.vip.personaengine.api.dto.FeedbackRequest
import ai.vip.personaengine.api.dto.FeedbackResponse
import ai.vip.personaengine.domain.chat.ChatRepository
import ai.vip.personaengine.domain.feedback.Feedback
import ai.vip.personaengine.domain.feedback.FeedbackRepository
import ai.vip.personaengine.domain.feedback.FeedbackStatus
import ai.vip.personaengine.domain.user.Role
import ai.vip.personaengine.domain.user.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FeedbackService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val feedbackRepository: FeedbackRepository
) {
    @Transactional
    fun createFeedback(email: String, request: FeedbackRequest): FeedbackResponse {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("사용자를 찾을 수 없습니다.")
        val chat = chatRepository.findById(request.chatId)
            .orElseThrow { EntityNotFoundException("해당 대화를 찾을 수 없습니다.") }

        if (chat.thread.user.id != user.id) {
            throw AccessDeniedException("이 대화에 피드백을 남길 권한이 없습니다.")
        }

        if (feedbackRepository.existsByUserAndChat(user, chat)) {
            throw IllegalArgumentException("이미 이 대화에 대한 피드백을 제출했습니다.")
        }

        val feedback = Feedback(
            user = user,
            chat = chat,
            isPositive = request.isPositive,
            status = FeedbackStatus.PENDING
        )

        val savedFeedback = feedbackRepository.save(feedback)
        return FeedbackResponse.from(savedFeedback)
    }

    fun listFeedbacks(email: String, isPositive: Boolean?, pageable: Pageable): Page<FeedbackResponse> {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("사용자를 찾을 수 없습니다.")

        val feedbacksPage: Page<Feedback> = if (user.role == Role.ADMIN) {
            if (isPositive != null) {
                feedbackRepository.findByIsPositive(isPositive, pageable)
            } else {
                feedbackRepository.findAll(pageable)
            }
        } else {
            if (isPositive != null) {
                feedbackRepository.findByUserAndIsPositive(user, isPositive, pageable)
            } else {
                feedbackRepository.findByUser(user, pageable)
            }
        }

        return feedbacksPage.map { FeedbackResponse.from(it) }
    }
}
