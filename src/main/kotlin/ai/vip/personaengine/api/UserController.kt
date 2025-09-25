package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.TokenResponse
import ai.vip.personaengine.api.dto.UserLoginRequest
import ai.vip.personaengine.api.dto.UserResponse
import ai.vip.personaengine.api.dto.UserSignUpRequest
import ai.vip.personaengine.application.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) {

    @PostMapping("/signup")
    fun signUp(@RequestBody request: UserSignUpRequest): ResponseEntity<UserResponse> {
        val userResponse = userService.signUp(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = userService.login(request)
        return ResponseEntity.ok(tokenResponse)
    }
}
