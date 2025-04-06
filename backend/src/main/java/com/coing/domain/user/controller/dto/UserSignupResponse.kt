package com.coing.domain.user.controller.dto

import java.util.UUID

data class UserSignupResponse(
    val message: String,
    val name: String,
    val email: String,
    val userId: UUID
)
