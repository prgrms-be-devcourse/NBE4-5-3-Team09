package com.coing.domain.bookmark.controller.dto

import jakarta.validation.constraints.NotBlank

data class BookmarkRequest(
    @field:NotBlank(message = "{coinCode.required}")
    val coinCode: String
)
