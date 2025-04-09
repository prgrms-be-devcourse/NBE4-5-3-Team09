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
import java.util.*
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

        // AtomicLong을 이용하여 "임시" ID 생성
        val newId = UUID.randomUUID().toString()
        val message = ChatMessage(
            id = newId,
            chatRoom = chatRoom,
            sender = sender,
            content = content,
            timestamp = LocalDateTime.now()
        )

        // 캐시에 메시지 저장
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
        // DB 저장: 만약 DB에 같은 id가 없다면 INSERT, 있으면 UPDATE로 동작
        val persistedMessage = chatMessageRepository.save(chatMessage)
        log.info("Persisted reported message {} for chat room {}", persistedMessage.id, chatMessage.chatRoom?.id)

        // 캐시에서 제거 or 상태 업데이트
        chatMessage.chatRoom?.id?.let { roomId ->
            chatMessageCache.getIfPresent(roomId)?.let { currentMessages ->
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
    fun findMessageById(messageId: String): ChatMessage? {
        // 우선 캐시에서 찾고, 없으면 DB에서 조회
        chatMessageCache.asMap().values.forEach { messages ->
            messages.find { it.id == messageId }?.let { return it }
        }
        return chatMessageRepository.findById(messageId).orElse(null)
    }
}
