package com.coing.global.config

import com.coing.domain.chat.entity.ChatMessage
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class ChatCacheConfig {

	/**
	 * 채팅 메시지 캐시 빈
	 * - Key: 채팅방 ID (Long)
	 * - Value: 해당 채팅방의 메시지 목록 (List<ChatMessage>)
	 *   -> 실제 값은 스레드 세이프한 CopyOnWriteArrayList를 사용합니다.
	 * - 15분 후 만료
	 */
	@Bean("chatMessageCache")
	fun chatMessageCache(): Cache<Long, List<ChatMessage>> {
		return Caffeine.newBuilder()
			.expireAfterWrite(15, TimeUnit.MINUTES)
			.initialCapacity(100)
			.maximumSize(1000)
			.build()
	}
}
