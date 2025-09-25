package ai.vip.personaengine.api.dto

data class UserActivityResponse(
    val signUpCount: Long,
    val loginCount: Long,
    val chatCount: Long
)
