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
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*

@Component
class JwtAuthenticationFilter : OncePerRequestFilter() {

	@Value("\${custom.jwt.secret-key}")
	private lateinit var secretKey: String

	private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

	@Throws(ServletException::class, IOException::class)
	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain
	) {
		val authorizationHeader = request.getHeader("Authorization")

		if (authorizationHeader.isNullOrEmpty() || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response)
			return
		}

		val token = authorizationHeader.substring(7)

		if (!isValidToken(secretKey, token)) {
			filterChain.doFilter(request, response)
			return
		}

		// JWT에서 클레임 값 가져오기
		val claims = getPayload(secretKey, token)
		val id = UUID.fromString(claims["id"].toString())

		// 토큰에 저장된 권한 값을 그대로 사용 (예: "ROLE_ADMIN" 또는 "ROLE_USER")
		val authorityClaim = claims["authority"]?.toString() ?: "ROLE_USER"
		val grantedAuthority = SimpleGrantedAuthority(authorityClaim)

		// CustomUserPrincipal 생성 (사용자 ID와 권한 정보 포함)
		val principal = CustomUserPrincipal(id, listOf(grantedAuthority))
		val authentication = UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
		SecurityContextHolder.getContext().authentication = authentication

		filterChain.doFilter(request, response)
	}
}
