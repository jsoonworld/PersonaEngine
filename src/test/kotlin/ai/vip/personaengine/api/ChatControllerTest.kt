package ai.vip.personaengine.api

import ai.vip.personaengine.api.dto.ChatRequest
import ai.vip.personaengine.domain.chat.Chat
import ai.vip.personaengine.domain.chat.ChatRepository
import ai.vip.personaengine.domain.chat.ChatThread
import ai.vip.personaengine.domain.chat.ChatThreadRepository
import ai.vip.personaengine.domain.user.Role
import ai.vip.personaengine.domain.user.User
import ai.vip.personaengine.domain.user.UserRepository
import ai.vip.personaengine.global.jwt.JwtTokenProvider
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
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
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val chatThreadRepository: ChatThreadRepository,
    private val chatRepository: ChatRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private lateinit var testUser: User
    private lateinit var testUserToken: String
    private lateinit var latestThread: ChatThread

    @BeforeEach
    fun setup() {
        chatRepository.deleteAll()
        chatThreadRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(User("test@example.com", "password", "testUser", Role.MEMBER))
        testUserToken = jwtTokenProvider.generateToken(testUser.email, testUser.role.name)

        val thread1 = chatThreadRepository.save(ChatThread(user = testUser, lastChatAt = LocalDateTime.now().minusDays(1)))
        chatRepository.save(Chat(thread = thread1, question = "오래된 질문", answer = "오래된 답변"))

        latestThread = chatThreadRepository.save(ChatThread(user = testUser, lastChatAt = LocalDateTime.now()))
        chatRepository.save(Chat(thread = latestThread, question = "최신 질문 1", answer = "최신 답변 1"))
        chatRepository.save(Chat(thread = latestThread, question = "최신 질문 2", answer = "최신 답변 2"))
    }

    @Test
    @DisplayName("대화 생성 성공")
    fun createChat_success() {
        // given
        val request = ChatRequest(question = "새로운 질문입니다.")

        // when & then
        mockMvc.post("/api/chats") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.question") { value("새로운 질문입니다.") }
            jsonPath("$.answer") { value("[Mock Response] '새로운 질문입니다.'에 대한 답변입니다.") }
            jsonPath("$.id") { exists() }
        }
    }

    @Test
    @DisplayName("대화 생성 실패 - 인증되지 않은 사용자")
    fun createChat_fail_unauthorized() {
        // given
        val request = ChatRequest(question = "인증 없이 보내는 질문")

        // when & then
        mockMvc.post("/api/chats") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @DisplayName("대화 목록 조회 성공 - 페이지네이션 및 정렬 확인")
    fun listChats_success() {
        // when & then
        mockMvc.get("/api/chats") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
            param("page", "0")
            param("size", "5")
            param("sort", "createdAt,desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content", Matchers.hasSize<Any>(2))
            jsonPath("$.totalElements") { value(2) }
            jsonPath("$.content[0].threadId") { value(latestThread.id) }
            jsonPath("$.content[0].chats", Matchers.hasSize<Any>(2))
            jsonPath("$.content[0].chats[0].question") { value("최신 질문 1") }
        }
    }
}
