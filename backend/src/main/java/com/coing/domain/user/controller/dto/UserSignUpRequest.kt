package com.coing.domain.user.controller.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserSignUpRequest(
    @field:NotBlank(message = "{name.required}")
    @field:Size(min = 2, max = 20, message = "{invalid.name.length}")
    val name: String,

    @field:NotBlank(message = "{email.required}")
    @field:Email(message = "{invalid.email.format}")
    val email: String,

    @field:NotBlank(message = "{password.required}")
    @field:Size(min = 8, max = 20, message = "{invalid.password.length}")
    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$",
        message = "{invalid.password.format}"
    )
    val password: String,

    @field:NotBlank(message = "{invalid.password.confirm}")
    val passwordConfirm: String
)
