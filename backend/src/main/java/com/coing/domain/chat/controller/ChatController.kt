package com.coing.domain.chat.controller

import com.coing.domain.chat.dto.ChatMessageDto
import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.entity.ChatRoom
import com.coing.domain.chat.service.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneOffset

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    // 채팅방의 메시지 목록 조회 (marketCode 기준)
    @GetMapping("/rooms/{marketCode}/messages")
    fun getMessagesByMarket(@PathVariable("marketCode") marketCode: String): ResponseEntity<List<ChatMessageDto>> {
        // 채팅방 조회 (없으면 생성)
        val chatRoom: ChatRoom = chatService.getOrCreateChatRoomByMarketCode(marketCode)
        // 채팅방의 ID는 null이 아님을 보장한다고 가정
        val messages: List<ChatMessage> = chatService.getMessages(chatRoom.id!!)

        // 엔티티를 DTO로 변환하여 반환
        val dtos: List<ChatMessageDto> = messages.map { message ->
            ChatMessageDto(
                sender = message.sender?.name ?: "",
                content = message.content,
                timestamp = message.timestamp?.toInstant(ZoneOffset.UTC)?.toEpochMilli()?.toString() ?: ""
            )
        }
        return ResponseEntity.ok(dtos)
    }
}
