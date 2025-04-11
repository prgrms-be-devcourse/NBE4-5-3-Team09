package com.coing.domain.user.controller.dto

import com.coing.domain.user.entity.Authority
import com.coing.domain.user.entity.Provider
import com.coing.domain.user.entity.User

data class UserInfoResponse(
    val name: String,
    val email: String,
    val authority: Authority = Authority.ROLE_USER,
    val provider: Provider = Provider.EMAIL
) {
    companion object {
        fun from(user: User): UserInfoResponse {
            return UserInfoResponse(
                name = user.name,
                email = user.email,
                authority = user.authority ?: Authority.ROLE_USER,
                provider = user.provider
            )
        }
    }
}
