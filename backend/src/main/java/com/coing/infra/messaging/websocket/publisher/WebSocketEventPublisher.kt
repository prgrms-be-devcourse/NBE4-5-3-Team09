package com.coing.infra.messaging.websocket.publisher

import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.common.port.CodeDto
import org.springframework.messaging.simp.SimpMessageSendingOperations

class WebSocketEventPublisher<T>(
    private val messagingTemplate: SimpMessageSendingOperations,
    private val destinationPrefix: String
): EventPublisher<T> where T : CodeDto {

    override fun publish(event: T) {
        val destination = "$destinationPrefix/${event.code}"
        messagingTemplate.convertAndSend(destination, event)
    }
}