package com.coing.domain.user.controller.dto

import com.coing.global.annotation.NoArg
import java.util.UUID

@NoArg
data class UserSignupResponse(
    val message: String,
    val name: String,
    val email: String,
    val userId: UUID
)
