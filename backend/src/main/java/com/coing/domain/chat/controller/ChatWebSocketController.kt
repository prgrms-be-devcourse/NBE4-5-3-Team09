package com.coing.domain.chat.controller

import com.coing.domain.chat.dto.ChatMessageDto
import com.coing.domain.chat.entity.ChatRoom
import com.coing.domain.chat.service.ChatHelperService
import com.coing.domain.chat.service.ChatService
import com.coing.domain.user.entity.User
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class ChatWebSocketController(
    private val chatService: ChatService,
    private val chatHelperService: ChatHelperService
) {

    private val log = LoggerFactory.getLogger(ChatWebSocketController::class.java)

    /**
     * 클라이언트는 /app/chat/{market}으로 메시지를 전송합니다.
     * 이 메시지는 /sub/coin/chat/{market}을 구독하는 모든 클라이언트에게 브로드캐스트됩니다.
     */
    @MessageMapping("/chat/{market}")
    @SendTo("/sub/coin/chat/{market}")
    @Throws(Exception::class)
    fun sendMessage(
        @DestinationVariable("market") market: String,
        @Payload message: ChatMessageDto,
        headerAccessor: StompHeaderAccessor
    ): ChatMessageDto? {
        // 토큰 추출 (tokenKey가 nullable이라면 String?로 선언)
        val tokenKey: String? = chatHelperService.extractTokenKey(headerAccessor)

        // 사용자 이름 할당
        val username: String = chatHelperService.getUserName(tokenKey)
        message.sender = username

        // 중복 메시지 체크
        if (chatHelperService.isDuplicateMessage(market, message.sender, message.content)) {
            return null
        }

        // 채팅방 조회 (없으면 생성)
        val chatRoom: ChatRoom = chatService.getOrCreateChatRoomByMarketCode(market)
        val chatRoomId: Long = chatRoom.id

        // User 엔티티 생성 (빌더 대신 Kotlin 기본 생성자 사용)
        val senderUser = User(
            id = tokenKey?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
            name = username
            // 필요한 다른 필드가 있다면 추가하세요.
        )

        // 채팅 메시지 캐시 저장
        val chatMessage = chatService.sendMessage(chatRoomId, senderUser, message.content)

        // DTO 변환 후 반환
        val resultDto: ChatMessageDto = chatHelperService.convertToDto(chatMessage)
        log.info("Received STOMP message for market [{}]: {}", market, resultDto)
        return resultDto
    }
}
