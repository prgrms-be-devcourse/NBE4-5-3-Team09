package com.coing.global.security.filter

import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.util.Ut.Jwt.getPayload
import com.coing.util.Ut.Jwt.isValidToken
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*

@Component
class JwtAuthenticationFilter : OncePerRequestFilter() {

	@Value("\${custom.jwt.secret-key}")
	private lateinit var secretKey: String

	@Throws(ServletException::class, IOException::class)
	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain
	) {
		val authorizationHeader = request.getHeader("Authorization")

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response)
			return
		}

		val token = authorizationHeader.substring(7)

		if (!isValidToken(secretKey, token)) {
			filterChain.doFilter(request, response)
			return
		}

		val claims = getPayload(secretKey, token)
		val id = UUID.fromString(claims["id"].toString())

		val principal = CustomUserPrincipal(id)
		val authentication = UsernamePasswordAuthenticationToken(principal, null, null)
		SecurityContextHolder.getContext().authentication = authentication

		filterChain.doFilter(request, response)
	}
}
