package ai.vip.personaengine.api.dto

import ai.vip.personaengine.domain.user.Role
import ai.vip.personaengine.domain.user.User

data class UserSignUpRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: Role = Role.MEMBER
)

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: Role
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                email = user.email,
                name = user.name,
                role = user.role
            )
        }
    }
}

data class TokenResponse(
    val accessToken: String
)
