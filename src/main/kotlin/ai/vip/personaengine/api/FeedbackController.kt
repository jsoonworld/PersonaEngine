package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.FeedbackRequest
import ai.vip.personaengine.api.dto.FeedbackResponse
import ai.vip.personaengine.application.FeedbackService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @GetMapping
    fun listFeedbacks(
        @AuthenticationPrincipal email: String,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        @RequestParam(required = false) isPositive: Boolean?
    ): ResponseEntity<Page<FeedbackResponse>> {
        val response = feedbackService.listFeedbacks(email, isPositive, pageable)
        return ResponseEntity.ok(response)
    }
}
