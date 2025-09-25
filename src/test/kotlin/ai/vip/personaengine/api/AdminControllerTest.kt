package ai.vip.personaengine.api

import ai.vip.personaengine.domain.chat.Chat
import ai.vip.personaengine.domain.chat.ChatRepository
import ai.vip.personaengine.domain.chat.ChatThread
import ai.vip.personaengine.domain.chat.ChatThreadRepository
import ai.vip.personaengine.domain.user.Role
import ai.vip.personaengine.domain.user.User
import ai.vip.personaengine.domain.user.UserRepository
import ai.vip.personaengine.global.jwt.JwtTokenProvider
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val chatThreadRepository: ChatThreadRepository,
    private val chatRepository: ChatRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private lateinit var memberUser: User
    private lateinit var adminUser: User
    private lateinit var memberToken: String
    private lateinit var adminToken: String

    @BeforeEach
    fun setup() {
        chatRepository.deleteAll()
        chatThreadRepository.deleteAll()
        userRepository.deleteAll()

        memberUser = userRepository.save(User("member@example.com", "password", "member", Role.MEMBER))
        adminUser = userRepository.save(User("admin@example.com", "password", "admin", Role.ADMIN))

        memberToken = jwtTokenProvider.generateToken(memberUser.email, memberUser.role.name)
        adminToken = jwtTokenProvider.generateToken(adminUser.email, adminUser.role.name)

        val thread = chatThreadRepository.save(ChatThread(user = memberUser))
        chatRepository.save(Chat(thread = thread, question = "질문", answer = "답변"))
    }

    @Test
    @DisplayName("사용자 활동 조회 - ADMIN 성공")
    fun getUserActivity_asAdmin_succeeds() {
        mockMvc.get("/api/admin/activities") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.signUpCount") { value(2) }
            jsonPath("$.chatCount") { value(1) }
        }
    }

    @Test
    @DisplayName("사용자 활동 조회 - MEMBER 실패 (403 Forbidden)")
    fun getUserActivity_asMember_isForbidden() {
        mockMvc.get("/api/admin/activities") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $memberToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @DisplayName("CSV 보고서 생성 - ADMIN 성공")
    fun generateChatReport_asAdmin_succeeds() {
        mockMvc.get("/api/admin/reports/chats") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
            header { string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8") }
            content { string(containsString("Chat ID,User Email,Question,Answer,Created At")) }
        }
    }

    @Test
    @DisplayName("CSV 보고서 생성 - MEMBER 실패 (403 Forbidden)")
    fun generateChatReport_asMember_isForbidden() {
        mockMvc.get("/api/admin/reports/chats") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $memberToken")
        }.andExpect {
            status { isForbidden() }
        }
    }
}
