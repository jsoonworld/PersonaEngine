package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.ChatRequest
import ai.vip.personaengine.api.dto.ChatResponse
import ai.vip.personaengine.api.dto.ThreadWithChatsResponse
import ai.vip.personaengine.application.ChatService
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

    @GetMapping
    fun listChats(
        @AuthenticationPrincipal email: String,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<ThreadWithChatsResponse>> {
        val response = chatService.listUserChats(email, pageable)
        return ResponseEntity.ok(response)
    }
}
