package com.coing.global.config

import com.coing.domain.user.service.OAuth2UserService
import com.coing.global.security.CustomAuthenticationEntryPoint
import com.coing.global.security.filter.JwtAuthenticationFilter
import com.coing.global.security.handler.OAuth2LoginFailureHandler
import com.coing.global.security.handler.OAuth2LoginSuccessHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
	private val jwtAuthenticationFilter: JwtAuthenticationFilter,
	private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
	private val oAuth2UserService: OAuth2UserService,
	private val successHandler: OAuth2LoginSuccessHandler,
	private val failureHandler: OAuth2LoginFailureHandler
) {

	@Value("\${next.server.url}")
	private lateinit var frontUrl: String

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.cors { it.configurationSource(corsConfigurationSource()) }
			.csrf(AbstractHttpConfigurer<*, *>::disable)
			.headers { it.frameOptions { frameOptions -> frameOptions.disable() } }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.authorizeHttpRequests { auth ->
				auth
					.requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
				    .anyRequest().permitAll()
			}
			.exceptionHandling { it.authenticationEntryPoint(customAuthenticationEntryPoint) }
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
			.oauth2Login { oauth2 ->
				oauth2
					.userInfoEndpoint { userInfo -> userInfo.userService(oAuth2UserService) }
					.successHandler(successHandler)
					.failureHandler(failureHandler)
			}

		return http.build()
	}

	@Bean
	fun corsConfigurationSource(): CorsConfigurationSource {
		val configuration = CorsConfiguration()
		configuration.allowedOriginPatterns = listOf(frontUrl)
		configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
		configuration.allowedHeaders = listOf("*")
		configuration.allowCredentials = true
		configuration.addExposedHeader("Authorization")

		val source = UrlBasedCorsConfigurationSource()
		source.registerCorsConfiguration("/**", configuration)
		return source
	}

	@Bean
	fun passwordEncoder(): PasswordEncoder {
		return Argon2PasswordEncoder(16, 32, 1, 4096, 3)
	}
}
