package com.coing.domain.user.dto

import java.util.UUID

data class CustomUserPrincipal(val id: UUID) {
    override fun toString(): String = id.toString()
}
