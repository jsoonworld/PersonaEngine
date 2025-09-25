package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.FeedbackRequest
import ai.vip.personaengine.domain.chat.Chat
import ai.vip.personaengine.domain.chat.ChatRepository
import ai.vip.personaengine.domain.chat.ChatThread
import ai.vip.personaengine.domain.chat.ChatThreadRepository
import ai.vip.personaengine.domain.feedback.Feedback
import ai.vip.personaengine.domain.feedback.FeedbackRepository
import ai.vip.personaengine.domain.feedback.FeedbackStatus
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
import org.springframework.test.web.servlet.get
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
    private lateinit var adminUser: User
    private lateinit var testUserToken: String
    private lateinit var adminUserToken: String
    private lateinit var chatOfTestUser: Chat

    @BeforeEach
    fun setup() {
        feedbackRepository.deleteAll()
        chatRepository.deleteAll()
        chatThreadRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(User("test@example.com", "password", "testUser", Role.MEMBER))
        testUserToken = jwtTokenProvider.generateToken(testUser.email, testUser.role.name)

        adminUser = userRepository.save(User("admin@example.com", "password", "adminUser", Role.ADMIN))
        adminUserToken = jwtTokenProvider.generateToken(adminUser.email, adminUser.role.name)

        val thread = chatThreadRepository.save(ChatThread(user = testUser))
        chatOfTestUser = chatRepository.save(Chat(thread = thread, question = "질문", answer = "답변"))

        feedbackRepository.save(Feedback(testUser, chatOfTestUser, true, FeedbackStatus.PENDING))
        feedbackRepository.save(Feedback(adminUser, chatOfTestUser, false, FeedbackStatus.PENDING))
    }

    @Test
    @DisplayName("피드백 생성 성공")
    fun createFeedback_success() {
        // given
        val chatForNewFeedback = chatRepository.save(Chat(thread = chatOfTestUser.thread, question = "다른 질문", answer = "다른 답변"))
        val request = FeedbackRequest(chatId = chatForNewFeedback.id, isPositive = true)

        // when & then
        mockMvc.post("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.chatId") { value(chatForNewFeedback.id) }
            jsonPath("$.userId") { value(testUser.id) }
        }
    }

    @Test
    @DisplayName("피드백 생성 실패 - 다른 사용자의 대화에 피드백 시도")
    fun createFeedback_fail_accessDenied() {
        // given
        val request = FeedbackRequest(chatId = chatOfTestUser.id, isPositive = true)

        // when & then
        mockMvc.post("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $adminUserToken")
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

    @Test
    @DisplayName("피드백 목록 조회 - 일반 사용자는 자신의 피드백만 조회")
    fun listFeedbacks_asMember_seesOnlyOwnFeedback() {
        mockMvc.get("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.content[0].userId") { value(testUser.id) }
        }
    }

    @Test
    @DisplayName("피드백 목록 조회 - 관리자는 모든 피드백을 조회")
    fun listFeedbacks_asAdmin_seesAllFeedbacks() {
        mockMvc.get("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $adminUserToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(2) }
        }
    }

    @Test
    @DisplayName("피드백 목록 조회 - 긍정 피드백 필터링")
    fun listFeedbacks_withPositiveFilter() {
        mockMvc.get("/api/feedbacks") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $adminUserToken")
            param("isPositive", "true")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.content[0].isPositive") { value(true) }
            jsonPath("$.content[0].userId") { value(testUser.id) }
        }
    }
}
