package com.coing.global.security.handler

import com.coing.domain.user.dto.CustomOAuth2User
import com.coing.domain.user.service.AuthTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*
import org.slf4j.LoggerFactory

@Component
class OAuth2LoginSuccessHandler(
	private val authTokenService: AuthTokenService
) : AuthenticationSuccessHandler {

	@Value("\${next.server.url}")
	private lateinit var frontUrl: String

	private val log = LoggerFactory.getLogger(this::class.java)

	@Throws(IOException::class)
	override fun onAuthenticationSuccess(
		request: HttpServletRequest,
		response: HttpServletResponse,
		authentication: Authentication
	) {
		val oAuth2User = authentication.principal as CustomOAuth2User
		val tempToken = UUID.randomUUID().toString()

		val cacheKey = "tempToken:$tempToken"
		authTokenService.setTempToken(cacheKey, oAuth2User.user.id.toString())

		log.info("[Social Login] Login succeed and issued tempToken")

		val redirectUrl = "$frontUrl/user/social-login/redirect?tempToken=$tempToken"
		response.sendRedirect(redirectUrl)
	}
}
