package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.ChatRequest
import ai.vip.personaengine.api.dto.ChatResponse
import ai.vip.personaengine.application.ChatService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService
) {
    @PostMapping
    fun createChat(
        @AuthenticationPrincipal email: String,
        @RequestBody request: ChatRequest
    ): ResponseEntity<ChatResponse> {
        val response = chatService.createChat(email, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
