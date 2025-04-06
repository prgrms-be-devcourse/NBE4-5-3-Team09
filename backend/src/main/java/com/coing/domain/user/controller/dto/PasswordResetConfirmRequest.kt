package com.coing.domain.user.controller.dto

import com.coing.global.annotation.NoArg

@NoArg
data class PasswordResetConfirmRequest(
    val newPassword: String,
    val newPasswordConfirm: String
)
