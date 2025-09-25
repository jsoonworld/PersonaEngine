package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.UserActivityResponse
import ai.vip.personaengine.application.AnalysisService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val analysisService: AnalysisService
) {
    @GetMapping("/activities")
    fun getUserActivity(): ResponseEntity<UserActivityResponse> {
        val response = analysisService.getUserActivity()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/reports/chats")
    fun generateChatReport(): ResponseEntity<String> {
        val csvContent = analysisService.generateChatReport()

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"chat_report.csv\"")

        return ResponseEntity(csvContent, headers, HttpStatus.OK)
    }
}
