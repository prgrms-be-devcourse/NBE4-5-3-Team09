package com.coing.domain.coin.common.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class PagedResponse<T>(
    @field:NotNull
    val page: Int,

    @field:NotNull
    val size: Int,

    @field:NotNull
    val totalElements: Long,

    @field:NotNull
    val totalPages: Int,

    @field:NotEmpty
    val content: List<T>
)