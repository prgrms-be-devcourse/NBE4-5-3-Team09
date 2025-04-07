package com.coing.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*

object Ut {
	private val objectMapper = ObjectMapper()

	object Jwt {
		fun createToken(keyString: String, expireSeconds: Int, claims: Map<String, Any>): String {
			val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())
			val issuedAt = Date()
			val expiration = Date(issuedAt.time + 1000L * expireSeconds)

			return Jwts.builder()
				.claims(claims)
				.issuedAt(issuedAt)
				.expiration(expiration)
				.signWith(secretKey)
				.compact()
		}

		fun isValidToken(keyString: String, token: String): Boolean {
			return try {
				val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())
				Jwts.parser().verifyWith(secretKey).build().parse(token)
				true
			} catch (e: Exception) {
				e.printStackTrace()
				false
			}
		}

		fun getPayload(keyString: String, jwtStr: String): Map<String, Any> {
			val secretKey = Keys.hmacShaKeyFor(keyString.toByteArray())
			val payload = Jwts.parser().verifyWith(secretKey).build().parse(jwtStr).payload
			@Suppress("UNCHECKED_CAST")
			return payload as Map<String, Any>
		}
	}

	object Json {
		fun toString(obj: Any): String {
			return try {
				objectMapper.writeValueAsString(obj)
			} catch (e: JsonProcessingException) {
				throw RuntimeException(e)
			}
		}
	}

	fun verifyToken(secretKey: String, token: String): Map<String, Any>? {
		return try {
			val sKey = Keys.hmacShaKeyFor(secretKey.toByteArray())
			val claims: Claims = Jwts.parser().verifyWith(sKey).build().parseSignedClaims(token).payload
			HashMap(claims)
		} catch (e: JwtException) {
			null
		}
	}

	object AuthTokenUtil {
		fun createEmailVerificationToken(secretKey: String, userId: UUID): String {
			val claims = hashMapOf("id" to userId.toString())
			val expireSeconds = 600
			return Jwt.createToken(secretKey, expireSeconds, claims)
		}
	}
}
