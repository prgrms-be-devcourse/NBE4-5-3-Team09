package com.coing.domain.user.controller.dto

import com.coing.global.annotation.NoArg

@NoArg
data class PasswordResetRequest(
    val email: String
)
