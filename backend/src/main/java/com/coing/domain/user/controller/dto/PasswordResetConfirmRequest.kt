package com.coing.domain.user.controller.dto

data class PasswordResetConfirmRequest(
    val newPassword: String,
    val newPasswordConfirm: String
)
