package com.coing.domain.chat.service

import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.entity.ChatRoom
import com.coing.domain.chat.repository.ChatRoomRepository
import com.coing.domain.chat.repository.ChatMessageRepository
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.service.MarketService
import com.coing.domain.user.entity.User
import com.coing.domain.user.entity.Provider
import com.github.benmanes.caffeine.cache.Cache
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.*

internal class ChatServiceUnitTest {

    private lateinit var chatRoomRepository: ChatRoomRepository
    private lateinit var chatMessageRepository: ChatMessageRepository
    private lateinit var marketService: MarketService
    private lateinit var chatMessageCache: Cache<Long, List<ChatMessage>>
    private lateinit var chatService: ChatService

    @BeforeEach
    fun `테스트 환경 초기화`() {
        chatRoomRepository = mock()
        chatMessageRepository = mock()
        marketService = mock()
        chatMessageCache = mock()
        chatService = ChatService(chatRoomRepository, chatMessageRepository, marketService, chatMessageCache)
    }

    @Test
    fun `채팅방이 존재하면 기존 채팅방 반환`() {
        val marketCode = "KRW-BTC"
        val market = Market(code = marketCode, koreanName = "비트코인", englishName = "Bitcoin")
        val chatRoom = ChatRoom(id = 1L, market = market, name = "BTC Chat")
        whenever(chatRoomRepository.findByMarketCode(marketCode)).thenReturn(Optional.of(chatRoom))

        val result = chatService.getOrCreateChatRoomByMarketCode(marketCode)
        assertThat(result).isEqualTo(chatRoom)
        verify(chatRoomRepository, never()).save(any())
    }

    @Test
    fun `채팅방이 없으면 새로운 채팅방 생성`() {
        val marketCode = "KRW-ETH"
        whenever(chatRoomRepository.findByMarketCode(marketCode)).thenReturn(Optional.empty())
        val market = Market(code = marketCode, koreanName = "이더리움", englishName = "Ethereum")
        whenever(marketService.getCachedMarketByCode(marketCode)).thenReturn(market)
        val newChatRoom = ChatRoom(id = 2L, market = market, name = "채팅방")
        whenever(chatRoomRepository.save(any())).thenReturn(newChatRoom)

        val result = chatService.getOrCreateChatRoomByMarketCode(marketCode)
        assertThat(result).isEqualTo(newChatRoom)
        verify(chatRoomRepository, times(1)).save(any())
    }

    @Test
    fun `메시지 전송 시 캐시에 메시지 저장`() {
        val chatRoomId = 1L
        val sender = User(
            id = UUID.randomUUID(),
            name = "Alice",
            email = "alice@example.com",
            password = "encoded",
            provider = Provider.EMAIL,
            verified = true
        )
        val content = "Hello"
        val market = Market(code = "KRW-BTC", koreanName = "비트코인", englishName = "Bitcoin")
        val chatRoom = ChatRoom(id = chatRoomId, market = market, name = "ChatRoom")
        whenever(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom))
        // 기존 캐시에 아무 메시지도 없다고 가정
        whenever(chatMessageCache.getIfPresent(chatRoomId)).thenReturn(null)

        val message = chatService.sendMessage(chatRoomId, sender, content)
        assertThat(message.chatRoom).isEqualTo(chatRoom)
        assertThat(message.sender).isEqualTo(sender)
        assertThat(message.content).isEqualTo(content)

        // 캐시 업데이트 검증: 새로 생성된 리스트에 message가 포함되어 있는지 확인
        verify(chatMessageCache, times(1)).put(eq(chatRoomId), argThat { this.contains(message) })
    }

    @Test
    fun `캐시에서 메시지 조회`() {
        val chatRoomId = 1L
        val messageList = listOf(
            ChatMessage(id = 1L, chatRoom = null, sender = null, content = "Hi", timestamp = LocalDateTime.now())
        )
        whenever(chatMessageCache.getIfPresent(chatRoomId)).thenReturn(messageList)

        val result = chatService.getMessages(chatRoomId)
        assertThat(result).isEqualTo(messageList)
    }
}
