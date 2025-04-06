package com.coing.domain.chat.handler

import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.service.ChatService
import com.coing.domain.user.entity.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

@Component
class ChatWebSocketHandler(
    private val chatService: ChatService,
    private val objectMapper: ObjectMapper
) : WebSocketHandler {

    private val log = LoggerFactory.getLogger(ChatWebSocketHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("WebSocket connection established: {}", session.id)
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        try {
            // 클라이언트에서 JSON 형태로 { "roomId":Long, "senderId":UUID, "content":String } 전송
            val request = objectMapper.readValue(message.payload.toString(), ChatMessageRequest::class.java)
            val chatMessage: ChatMessage = chatService.sendMessage(
                request.roomId,
                // 빌더 대신 생성자 호출: User 엔티티에 기본값이 설정되어 있다면 id만 전달해도 됨
                User(id = request.senderId),
                request.content
            )
            // 클라이언트에 전송할 메시지(JSON 문자열) 생성
            val response = objectMapper.writeValueAsString(chatMessage)
            session.sendMessage(TextMessage(response))
        } catch (e: Exception) {
            log.error("Error handling WebSocket message", e)
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("Transport error: {} with exception", session.id, exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        log.info("WebSocket connection closed: {} with status {}", session.id, closeStatus)
    }

    override fun supportsPartialMessages(): Boolean = false

    // 내부 DTO for WebSocket 메시지 요청
    data class ChatMessageRequest(
        val roomId: Long,
        val senderId: java.util.UUID,
        val content: String
    )
}
