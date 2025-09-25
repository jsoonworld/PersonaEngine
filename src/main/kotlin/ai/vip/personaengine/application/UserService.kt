package ai.vip.personaengine.application

import ai.vip.personaengine.api.dto.TokenResponse
import ai.vip.personaengine.api.dto.UserLoginRequest
import ai.vip.personaengine.api.dto.UserResponse
import ai.vip.personaengine.api.dto.UserSignUpRequest
import ai.vip.personaengine.domain.user.User
import ai.vip.personaengine.domain.user.UserRepository
import ai.vip.personaengine.global.jwt.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Transactional
    fun signUp(request: UserSignUpRequest): UserResponse {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다.")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = request.role
        )

        val savedUser = userRepository.save(user)
        return UserResponse.from(savedUser)
    }

    fun login(request: UserLoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("가입되지 않은 이메일입니다.")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("잘못된 비밀번호입니다.")
        }

        val token = jwtTokenProvider.generateToken(user.email, user.role.name)
        return TokenResponse(accessToken = token)
    }
}
