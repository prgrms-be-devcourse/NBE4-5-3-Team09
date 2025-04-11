package com.coing.global.security.handler

import com.coing.domain.user.dto.CustomOAuth2User
import com.coing.domain.user.service.AuthTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.util.*
import org.slf4j.LoggerFactory

@Component
class OAuth2LoginSuccessHandler(
	private val authTokenService: AuthTokenService,
	@Value("\${next.server.url}") private val frontUrl: String
) : AuthenticationSuccessHandler {

	private val log = LoggerFactory.getLogger(this::class.java)

	override fun onAuthenticationSuccess(
		request: HttpServletRequest,
		response: HttpServletResponse,
		authentication: Authentication
	) {
		val oAuth2User = authentication.principal as CustomOAuth2User
		val state = request.getParameter("state")
		val tempToken = UUID.randomUUID().toString()

		val (prefix, logSuffix, queryParam) = when (state) {
			"delete_account" -> Triple("quitToken", "quitToken", "quitToken=$tempToken")
			else -> Triple("tempToken", "tempToken", "tempToken=$tempToken")
		}

		val cacheKey = "$prefix:$tempToken"
		authTokenService.setTempToken(cacheKey, oAuth2User.user.id.toString())

		log.info("[Social Login] Login succeed and issued $logSuffix")

		val redirectUrl = "$frontUrl/user/social-login/redirect?$queryParam"
		response.sendRedirect(redirectUrl)
	}
}
