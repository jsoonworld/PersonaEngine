package ai.vip.personaengine.infrastructure

import org.springframework.stereotype.Component

@Component
class OpenAiClient {
    fun createAnswer(question: String): String {
        return "[Mock Response] '${question}'에 대한 답변입니다."
    }
}
