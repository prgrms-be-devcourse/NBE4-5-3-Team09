package com.coing.domain.user.controller.dto

import com.coing.global.annotation.NoArg
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@NoArg
data class UserLoginRequest(
    @field:NotBlank(message = "{email.required}")
    @field:Email(message = "{invalid.email.format}")
    val email: String,

    @field:NotBlank(message = "{password.required}")
    val password: String
)
