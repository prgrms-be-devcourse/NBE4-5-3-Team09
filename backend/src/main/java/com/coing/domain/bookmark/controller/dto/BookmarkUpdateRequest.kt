package com.coing.domain.bookmark.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BookmarkUpdateRequest(
    @field:NotNull(message = "{bookmarkId.required}")
    val bookmarkId: Long,

    @field:NotBlank(message = "{code.required}")
    val code: String
)
