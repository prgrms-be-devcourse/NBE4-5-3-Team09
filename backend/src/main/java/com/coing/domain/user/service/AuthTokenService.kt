package com.coing.domain.user.service

import com.coing.domain.user.controller.dto.UserResponse
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.util.Ut
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthTokenService {

    @Value("\${custom.jwt.secret-key}")
    lateinit var jwtSecretKey: String

    @Value("\${custom.jwt.expire-seconds}")
    var jwtExpireSeconds: Int = 0

    @Value("\${custom.jwt.refresh-expire-seconds}")
    var jwtRefreshExpireSeconds: Int = 0

    private val log = LoggerFactory.getLogger(AuthTokenService::class.java)

    // 액세스 토큰 생성: UserResponse를 사용
    fun genAccessToken(userResponse: UserResponse): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["id"] = userResponse.id
        // claims["authority"] = userResponse.authority // 나중에 권한 관련 추가
        val token = Ut.Jwt.createToken(jwtSecretKey, jwtExpireSeconds, claims)
        log.info("JWT 액세스 토큰 생성: {}", userResponse.email)
        return token
    }

    // 리프레시 토큰 생성: UserResponse를 사용
    fun genRefreshToken(userResponse: UserResponse): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["id"] = userResponse.id
        val token = Ut.Jwt.createToken(jwtSecretKey, jwtRefreshExpireSeconds, claims)
        log.info("JWT 리프레시 토큰 생성: {}", userResponse.email)
        return token
    }

    // 액세스 토큰 생성: CustomUserPrincipal을 사용
    fun genAccessToken(principal: CustomUserPrincipal): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["id"] = principal.id
        val token = Ut.Jwt.createToken(jwtSecretKey, jwtExpireSeconds, claims)
        log.info("JWT 액세스 토큰 생성(CustomUserPrincipal): {}", principal.id)
        return token
    }

    // 리프레시 토큰 생성: CustomUserPrincipal을 사용
    fun genRefreshToken(principal: CustomUserPrincipal): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["id"] = principal.id
        val token = Ut.Jwt.createToken(jwtSecretKey, jwtRefreshExpireSeconds, claims)
        log.info("JWT 리프레시 토큰 생성(CustomUserPrincipal): {}", principal.id)
        return token
    }

    fun verifyToken(token: String): Map<String, Any>? {
        return try {
            Ut.verifyToken(jwtSecretKey, token)
        } catch (e: Exception) {
            log.error("토큰 검증 실패", e)
            null
        }
    }

    fun parseId(token: String): UUID? {
        val claims = verifyToken(token)
        if (claims == null || claims["id"] == null) {
            return null
        }
        return UUID.fromString(claims["id"].toString())
    }

    @CachePut(cacheNames = ["tempTokens"], key = "#root.args[0]")
    fun setTempToken(token: String, userId: String): String {
        return userId
    }

    @Cacheable(cacheNames = ["tempTokens"], key = "#root.args[0]")
    fun getUserIdWithTempToken(token: String): String? {
        return null
    }

    @CacheEvict(cacheNames = ["tempTokens"], key = "#root.args[0]")
    fun removeTempToken(token: String) {
    }
}
