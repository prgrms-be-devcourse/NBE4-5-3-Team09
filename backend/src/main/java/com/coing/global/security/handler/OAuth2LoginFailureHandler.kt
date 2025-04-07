package com.coing.global.security.handler

import com.coing.util.MessageUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

@Component
class OAuth2LoginFailureHandler(
	private val messageUtil: MessageUtil
) : AuthenticationFailureHandler {

	@Value("\${next.server.url}")
	private lateinit var frontUrl: String

	@Throws(IOException::class)
	override fun onAuthenticationFailure(
		request: HttpServletRequest,
		response: HttpServletResponse,
		exception: AuthenticationException
	) {
		val redirectUrl = UriComponentsBuilder
			.fromUriString("$frontUrl/user/social-login")
			.queryParam("error", messageUtil.resolveMessage("login.failure"))
			.toUriString()

		response.sendRedirect(redirectUrl)
	}
}
