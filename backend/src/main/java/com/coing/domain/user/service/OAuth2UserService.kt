package com.coing.domain.user.service

import com.coing.domain.user.dto.CustomOAuth2User
import com.coing.domain.user.dto.OAuth2UserDto
import com.coing.domain.user.entity.Provider
import com.coing.domain.user.entity.User
import com.coing.domain.user.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OAuth2UserService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val oAuth2UserDto = convertKakaoUserAttribute(oAuth2User)
        val user = oauth2Login(oAuth2UserDto)
        return CustomOAuth2User(user)
    }

    private fun convertKakaoUserAttribute(oAuth2User: OAuth2User): OAuth2UserDto {
        val attribute = oAuth2User.attributes

        val properties = attribute["properties"]
        val kakaoAccount = attribute["kakao_account"]

        if (properties !is Map<*, *> || kakaoAccount !is Map<*, *>) {
            throw IllegalArgumentException("invalid.kakao.attribute")
        }

        val nickname = properties["nickname"]?.toString()
            ?: throw IllegalArgumentException("nickname.not.found")

        val email = kakaoAccount["email"]?.toString()
            ?: throw IllegalArgumentException("email.not.found")

        return OAuth2UserDto.of(
            Provider.KAKAO,
            nickname,
            email
        )
    }

    private fun oauth2Login(dto: OAuth2UserDto): User {
        return userRepository.findByEmail(dto.email).orElseGet {
            val savedUser = User(
                name = dto.name,
                email = dto.email,
                password = UUID.randomUUID().toString(),
                verified = true,
                provider = dto.provider
            )
            userRepository.save(savedUser)
        }
    }
}
