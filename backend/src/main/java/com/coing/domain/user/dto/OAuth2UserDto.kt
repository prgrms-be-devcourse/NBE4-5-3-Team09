package com.coing.domain.user.dto

import com.coing.domain.user.entity.Provider

data class OAuth2UserDto(
	val provider: Provider,
	val name: String,
	val email: String
) {
	companion object {
		fun of(provider: Provider, name: String, email: String): OAuth2UserDto {
			return OAuth2UserDto(provider, name, email)
		}
	}
}
