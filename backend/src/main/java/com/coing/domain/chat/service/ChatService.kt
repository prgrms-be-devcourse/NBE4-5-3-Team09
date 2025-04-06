package com.coing.domain.chat.service

import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.entity.ChatRoom
import com.coing.domain.chat.repository.ChatMessageRepository
import com.coing.domain.chat.repository.ChatRoomRepository
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.service.MarketService
import com.coing.domain.user.entity.User
import com.github.benmanes.caffeine.cache.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

@Service
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val marketService: MarketService,
    private val chatMessageCache: Cache<Long, List<ChatMessage>>
) {

    private val messageIdSequence = AtomicLong(1)
    private val log = LoggerFactory.getLogger(ChatService::class.java)

    @Transactional
    fun getOrCreateChatRoomByMarketCode(marketCode: String): ChatRoom {
        return chatRoomRepository.findByMarketCode(marketCode).orElseGet {
            // 마켓 정보 조회
            val market: Market = marketService.getCachedMarketByCode(marketCode)
            // 새로운 ChatRoom 인스턴스 생성 (builder 없이 생성자 호출)
            val chatRoom = ChatRoom(
                market = market,
//                name = "${market.koreanName} 채팅방",   임시 주석처리
                name = "채팅방",
                createdAt = LocalDateTime.now()
            )
            chatRoomRepository.save(chatRoom)
        }
    }

    @Transactional
    fun sendMessage(chatRoomId: Long, sender: User, content: String): ChatMessage {
        val chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow { RuntimeException("Chat room not found") }

        // 새로운 ChatMessage 인스턴스 생성 (builder 대신 생성자 호출)
        val message = ChatMessage(
            id = messageIdSequence.getAndIncrement(),
            chatRoom = chatRoom,
            sender = sender,
            content = content,
            timestamp = LocalDateTime.now()
        )

        // 스레드 세이프한 리스트로 캐시에 저장
        val messages = chatMessageCache.getIfPresent(chatRoomId)?.toMutableList()
            ?: CopyOnWriteArrayList<ChatMessage>()
        messages.add(message)
        chatMessageCache.put(chatRoomId, messages)

        log.info("Cached message in chat room {}: {}", chatRoomId, message)
        return message
    }

    @Transactional(readOnly = true)
    fun getMessages(chatRoomId: Long): List<ChatMessage> {
        return chatMessageCache.getIfPresent(chatRoomId) ?: CopyOnWriteArrayList()
    }
}
