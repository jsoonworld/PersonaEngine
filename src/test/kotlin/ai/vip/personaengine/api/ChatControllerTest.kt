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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
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
    private lateinit var otherUser: User
    private lateinit var testUserToken: String
    private lateinit var otherUserToken: String
    private lateinit var threadOfTestUser: ChatThread

    @BeforeEach
    fun setup() {
        chatRepository.deleteAll()
        chatThreadRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(User("test@example.com", "password", "testUser", Role.MEMBER))
        testUserToken = jwtTokenProvider.generateToken(testUser.email, testUser.role.name)

        otherUser = userRepository.save(User("other@example.com", "password", "otherUser", Role.MEMBER))
        otherUserToken = jwtTokenProvider.generateToken(otherUser.email, otherUser.role.name)

        threadOfTestUser = chatThreadRepository.save(ChatThread(user = testUser, lastChatAt = LocalDateTime.now()))
        chatRepository.save(Chat(thread = threadOfTestUser, question = "질문", answer = "답변"))
    }

    @Test
    @DisplayName("대화 생성 성공")
    fun createChat_success() {
        val request = ChatRequest(question = "새로운 질문입니다.")
        mockMvc.post("/api/chats") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    @DisplayName("대화 생성 실패 - 인증되지 않은 사용자")
    fun createChat_fail_unauthorized() {
        val request = ChatRequest(question = "인증 없이 보내는 질문")
        mockMvc.post("/api/chats") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @DisplayName("대화 목록 조회 성공")
    fun listChats_success() {
        mockMvc.get("/api/chats") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
            param("page", "0")
            param("size", "5")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.content[0].threadId") { value(threadOfTestUser.id) }
        }
    }

    @Test
    @DisplayName("스레드 삭제 성공")
    fun deleteThread_success() {
        // when & then
        mockMvc.delete("/api/chats/threads/${threadOfTestUser.id}") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $testUserToken")
        }.andExpect {
            status { isNoContent() }
        }

        val foundThread = chatThreadRepository.findById(threadOfTestUser.id)
        assertThat(foundThread.isPresent).isFalse()
    }

    @Test
    @DisplayName("스레드 삭제 실패 - 다른 사용자의 스레드 삭제 시도")
    fun deleteThread_fail_accessDenied() {
        // when & then
        mockMvc.delete("/api/chats/threads/${threadOfTestUser.id}") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $otherUserToken")
        }.andExpect {
            status { isForbidden() }
        }

        val foundThread = chatThreadRepository.findById(threadOfTestUser.id)
        assertThat(foundThread.isPresent).isTrue()
    }
}
