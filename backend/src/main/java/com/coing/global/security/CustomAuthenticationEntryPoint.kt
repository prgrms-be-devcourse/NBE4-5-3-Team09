package com.coing.global.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

// 액세스 토큰 만료 시 403 Forbidden 응답을 반환하는 클래스
@Component
class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
	@Throws(IOException::class)
	override fun commence(
		request: HttpServletRequest,
		response: HttpServletResponse,
		authException: AuthenticationException
	) {
		// 403 응답을 반환하고 에러 메시지 작성
		response.status = HttpServletResponse.SC_FORBIDDEN
		response.writer.write("Forbidden: ${authException.message}")
	}
}
