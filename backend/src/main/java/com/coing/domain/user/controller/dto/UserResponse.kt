package com.coing.domain.user.controller.dto

import com.coing.domain.user.entity.User
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val name: String,
    val email: String,
    val verified: Boolean
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                name = user.name,
                email = user.email,
                verified = user.verified
            )
        }
    }
}
