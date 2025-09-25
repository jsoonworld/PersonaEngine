package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.FeedbackRequest
import ai.vip.personaengine.domain.chat.Chat
import ai.vip.personaengine.domain.chat.ChatRepository
import ai.vip.personaengine.domain.chat.ChatThread
import ai.vip.personaengine.domain.chat.ChatThreadRepository
import ai.vip.personaengine.domain.feedback.FeedbackRepository
import ai.vip.personaengine.domain.user.Role
import ai.vip.personaengine.domain.user.User
import ai.vip.personaengine.domain.user.UserRepository
import ai.vip.personaengine.global.jwt.JwtTokenProvider
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FeedbackControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val chatThreadRepository: ChatThreadRepository,
    private val chatRepository: ChatRepository,
    private val feedbackRepository: FeedbackRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private lateinit var testUser: User
    private lateinit var otherUser: User
    private lateinit var testUserToken: String
    private lateinit var otherUserToken: String
    private lateinit var chatOfTestUser: Chat

    @BeforeEach
    fun setup() {
        feedbackRepository.deleteAll()
        chatRepository.deleteAll()
        chatThreadRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(User("test@example.com", "password", "testUser", Role.MEMBER))
        testUserToken = jwtTokenProvider.generateToken(testUser.email, testUser.role.name)

        otherUser = userRepository.save(User("other@example.com", "password", "otherUser", Role.MEMBER))
        otherUserToken = jwtTokenProvider.generateToken(otherUser.email, otherUser.role.name)

        val thread = chatThreadRepository.save(ChatThread(user = testUser))
        chatOfTestUser = chatRepository.save(Chat(thread = thread, question = "질문", answer = "답변"))
    }

    @Test
    @DisplayName("피드백 생성 성공")
    fun createFeedback_success() {
        // given
        val request = FeedbackRequest(chatId = chatOfTestUser.id, isPositive = true)

        // when & then
        mockMvc.post("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.chatId") { value(chatOfTestUser.id) }
            jsonPath("$.userId") { value(testUser.id) }
            jsonPath("$.isPositive") { value(true) }
            jsonPath("$.status") { value("PENDING") }
        }
    }

    @Test
    @DisplayName("피드백 생성 실패 - 다른 사용자의 대화에 피드백 시도")
    fun createFeedback_fail_accessDenied() {
        // given
        val request = FeedbackRequest(chatId = chatOfTestUser.id, isPositive = true)

        // when & then
        mockMvc.post("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $otherUserToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @DisplayName("피드백 생성 실패 - 동일한 대화에 중복 피드백 시도")
    fun createFeedback_fail_duplicate() {
        // given
        val request = FeedbackRequest(chatId = chatOfTestUser.id, isPositive = true)

        mockMvc.post("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
        }

        // when & then
        mockMvc.post("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("이미 이 대화에 대한 피드백을 제출했습니다.") }
        }
    }
}
