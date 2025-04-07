package com.coing.domain.user.dto

import com.coing.domain.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomOAuth2User(val user: User) : OAuth2User {

    override fun getAttributes(): Map<String, Any>? = null

    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorities: MutableList<GrantedAuthority> = ArrayList()
        authorities.add(SimpleGrantedAuthority(user.authority!!.name))
        return authorities
    }

    override fun getName(): String = user.id.toString()
}
