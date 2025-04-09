package com.coing.domain.chat.controller

import com.coing.domain.chat.dto.ChatMessageDto
import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.entity.ChatRoom
import com.coing.domain.chat.service.ChatService
import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneOffset

@Tag(name = "Chat API", description = "채팅 관련 API 엔드포인트")
@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {
    // 채팅방의 메시지 목록 조회 (marketCode 기준, 최근 10분 메시지 캐시 조회)
    @Operation(summary = "채팅방 메세지 목록 조회 - 최근 10분")
    @ApiErrorCodeExamples(ErrorCode.MARKET_NOT_FOUND, ErrorCode.CHAT_ROOM_NOT_FOUND, ErrorCode.INTERNAL_SERVER_ERROR)
    @GetMapping("/rooms/{marketCode}/messages")
    fun getMessagesByMarket(@PathVariable("marketCode") marketCode: String): ResponseEntity<List<ChatMessageDto>> {
        // 채팅방 조회 (없으면 생성)
        val chatRoom: ChatRoom = chatService.getOrCreateChatRoomByMarketCode(marketCode)
        // 캐시에서 해당 채팅방의 메시지 리스트를 조회 (만약 캐시에 없다면 빈 리스트 반환)
        val messages: List<ChatMessage> = chatService.getMessages(chatRoom.id!!)

        // 최근 10분 이내의 메시지 필터링
        val cutoffTime = LocalDateTime.now().minusMinutes(10)
        val recentMessages = messages.filter { it.timestamp?.isAfter(cutoffTime) == true }

        // 엔티티를 DTO로 변환하여 반환
        val dtos: List<ChatMessageDto> = recentMessages.map { message ->
            ChatMessageDto(
                id = message.id,
                sender = message.sender?.name ?: "",
                content = message.content,
                timestamp = message.timestamp?.toInstant(ZoneOffset.UTC)?.toEpochMilli()?.toString() ?: ""
            )
        }
        return ResponseEntity.ok(dtos)
    }
}
