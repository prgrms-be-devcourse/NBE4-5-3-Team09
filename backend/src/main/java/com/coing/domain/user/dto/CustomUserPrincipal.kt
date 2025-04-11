package com.coing.domain.user.dto

import org.springframework.security.core.GrantedAuthority
import java.util.UUID

data class CustomUserPrincipal(
    val id: UUID,
    val authorities: List<GrantedAuthority> = emptyList()
) {
    override fun toString(): String = id.toString()
}
