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
    private val chatMessageRepository: ChatMessageRepository, // 생성자 주입
    private val marketService: MarketService,
    // 캐시에는 채팅방 ID별 메시지 리스트를 저장 (불변 리스트 타입)
    private val chatMessageCache: Cache<Long, List<ChatMessage>>
) {
    private val messageIdSequence = AtomicLong(1)
    private val log = LoggerFactory.getLogger(ChatService::class.java)

    @Transactional
    fun getOrCreateChatRoomByMarketCode(marketCode: String): ChatRoom {
        return chatRoomRepository.findByMarketCode(marketCode).orElseGet {
            // 마켓 정보 조회
            val market: Market = marketService.getCachedMarketByCode(marketCode)
            // 새로운 ChatRoom 인스턴스 생성
            val chatRoom = ChatRoom(
                market = market,
                name = "${market.koreanName} 채팅방"
            )
            chatRoomRepository.save(chatRoom)
        }
    }

    @Transactional
    fun sendMessage(chatRoomId: Long, sender: User, content: String): ChatMessage {
        val chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow { RuntimeException("Chat room not found") }

        // 새로운 ChatMessage 인스턴스 생성
        val message = ChatMessage(
            id = messageIdSequence.getAndIncrement(),
            chatRoom = chatRoom,
            sender = sender,
            content = content,
            timestamp = LocalDateTime.now()
        )

        // 캐시에 메시지 저장 (실시간 처리를 위해)
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

    // 신고 시 DB에 해당 메시지(신고 대상)를 바로 영구 저장하는 로직
    @Transactional
    fun persistReportedMessage(chatMessage: ChatMessage): ChatMessage {
        // 신고된 메시지를 DB에 저장
        val persistedMessage = chatMessageRepository.save(chatMessage)
        log.info("Persisted reported message {} for chat room {}", persistedMessage.id, chatMessage.chatRoom?.id)

        // 캐시에 저장되어 있다면, 해당 메시지를 제거하여 중복 저장을 방지합니다.
        chatMessage.chatRoom?.id?.let { roomId ->
            chatMessageCache.getIfPresent(roomId)?.let { currentMessages ->
                // List가 불변일 수 있으므로, 변경 가능한 리스트로 변환 후 삭제
                val mutableMessages = currentMessages.toMutableList()
                val removed = mutableMessages.removeIf { it.id == chatMessage.id }
                if (removed) {
                    chatMessageCache.put(roomId, mutableMessages)
                    log.info("Removed reported message {} from cache for chat room {}", chatMessage.id, roomId)
                }
            }
        }
        return persistedMessage
    }

    @Transactional(readOnly = true)
    fun findMessageById(messageId: Long): ChatMessage? {
        // 우선 캐시에서 찾고, 없으면 DB에서 조회
        chatMessageCache.asMap().values.forEach { messages ->
            messages.find { it.id == messageId }?.let { return it }
        }
        return chatMessageRepository.findById(messageId).orElse(null)
    }
}
