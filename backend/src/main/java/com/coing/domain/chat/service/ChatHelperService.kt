package com.coing.domain.chat.service

import com.coing.domain.chat.dto.ChatMessageDto
import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.user.service.AuthTokenService
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class ChatHelperService(
    private val authTokenService: AuthTokenService
) {

    private val userCounter = AtomicInteger(1)
    private val userNameMap = ConcurrentHashMap<String, String>()

    private val recentMessages = ConcurrentHashMap<String, Long>()
    private val DUPLICATE_THRESHOLD_MS = 500L

    /**
     * StompHeaderAccessor에서 Authorization 헤더를 추출하여 토큰의 "id" 클레임을 반환합니다.
     * 토큰이 없거나 디코딩에 실패하면 null을 반환합니다.
     */
    fun extractTokenKey(headerAccessor: StompHeaderAccessor): String? {
        val authHeader = headerAccessor.getFirstNativeHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            try {
                val claims: Map<String, Any>? = authTokenService.verifyToken(token)
                if (claims?.get("id") != null) {
                    return claims["id"].toString()
                }
            } catch (e: Exception) {
                log.error("JWT 디코딩 실패: {}", e.message)
            }
        }
        return null
    }

    /**
     * ChatMessage 엔티티를 ChatMessageDto로 변환합니다.
     */
    fun convertToDto(chatMessage: ChatMessage): ChatMessageDto {
        return ChatMessageDto(
            sender = chatMessage.sender?.name ?: "",
            content = chatMessage.content,
            timestamp = chatMessage.timestamp?.toInstant(ZoneOffset.UTC)?.toEpochMilli()?.toString() ?: ""
        )
    }

    /**
     * 동일 채팅방(market)에서 동일한 sender와 content를 가진 메시지가 일정 시간 이내에 발생했는지 체크합니다.
     * 중복이면 true를 반환합니다.
     */
    fun isDuplicateMessage(market: String, sender: String, content: String): Boolean {
        val key = "$market:$sender:$content"
        val now = System.currentTimeMillis()
        val lastTime = recentMessages[key]
        if (lastTime != null && (now - lastTime) < DUPLICATE_THRESHOLD_MS) {
            log.warn("Duplicate message detected for key {}: Ignoring", key)
            return true
        }
        recentMessages[key] = now
        return false
    }

    /**
     * 토큰 키를 이용해 사용자 이름을 할당합니다.
     * 토큰 키가 없으면 "Anonymous"를 반환합니다.
     */
    fun getUserName(tokenKey: String?): String {
        return if (tokenKey != null) {
            userNameMap.computeIfAbsent(tokenKey) { "User${userCounter.getAndIncrement()}" }
        } else {
            "Anonymous"
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ChatHelperService::class.java)
    }
}
