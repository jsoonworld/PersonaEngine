package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.UserLoginRequest
import ai.vip.personaengine.api.dto.UserSignUpRequest
import ai.vip.personaengine.domain.user.Role
import ai.vip.personaengine.domain.user.User
import ai.vip.personaengine.domain.user.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @BeforeEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("회원가입 성공")
    fun signUp_success() {
        // given
        val request = UserSignUpRequest(
            email = "test@example.com",
            password = "password123",
            name = "홍길동"
        )

        // when & then
        mockMvc.post("/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.email") { value("test@example.com") }
            jsonPath("$.name") { value("홍길동") }
            jsonPath("$.id") { exists() }
        }
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    fun signUp_fail_duplicateEmail() {
        // given
        val existingUser = User("test@example.com", "password", "기존유저", Role.MEMBER)
        userRepository.save(existingUser)

        val request = UserSignUpRequest(
            email = "test@example.com",
            password = "password123",
            name = "홍길동"
        )

        // when & then
        mockMvc.post("/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("이미 사용 중인 이메일입니다.") }
        }
    }

    @Test
    @DisplayName("로그인 성공")
    fun login_success() {
        // given
        val user = User(
            email = "test@example.com",
            password = passwordEncoder.encode("password123"),
            name = "홍길동",
            role = Role.MEMBER
        )
        userRepository.save(user)

        val request = UserLoginRequest("test@example.com", "password123")

        // when & then
        mockMvc.post("/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { exists() }
            jsonPath("$.accessToken") { isNotEmpty() }
        }
    }
}
