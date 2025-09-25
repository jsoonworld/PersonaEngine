package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.FeedbackRequest
import ai.vip.personaengine.api.dto.FeedbackResponse
import ai.vip.personaengine.application.FeedbackService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(
    private val feedbackService: FeedbackService
) {
    @PostMapping
    fun createFeedback(
        @AuthenticationPrincipal email: String,
        @RequestBody request: FeedbackRequest
    ): ResponseEntity<FeedbackResponse> {
        val response = feedbackService.createFeedback(email, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
