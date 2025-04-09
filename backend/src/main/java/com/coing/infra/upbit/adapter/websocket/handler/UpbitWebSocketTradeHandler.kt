package com.coing.infra.upbit.adapter.websocket.handler

import com.coing.infra.upbit.adapter.websocket.UpbitWebSocketEventAdapter
import com.coing.infra.upbit.adapter.websocket.dto.UpbitWebSocketTradeDto
import com.coing.infra.upbit.adapter.websocket.enums.EnumUpbitWebSocketRequestType
import com.coing.infra.upbit.util.UpbitRequestBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import java.nio.charset.StandardCharsets

@Component
class UpbitWebSocketTradeHandler(
    private val upbitWebSocketEventAdapter: UpbitWebSocketEventAdapter,
    private val upbitRequestBuilder: UpbitRequestBuilder,
    private val objectMapper: ObjectMapper = ObjectMapper()
) : BinaryWebSocketHandler() {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("Upbit WebSocket Trade connection established.")
        val subscribeMessage = upbitRequestBuilder.makeWebSocketRequest(EnumUpbitWebSocketRequestType.TRADE)
        session.sendMessage(TextMessage(subscribeMessage))
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val payload = String(message.payload.array(), StandardCharsets.UTF_8)
        if (payload.isNotEmpty()) processMessage(payload)
    }

    private fun processMessage(payload: String) {
        try {
            if ("{\"status\":\"UP\"}" == payload) {
                log.debug("Received keepalive message: {}", payload)
                return
            }

            val tradeDto = objectMapper.readValue(payload, UpbitWebSocketTradeDto::class.java)
            upbitWebSocketEventAdapter.handleTradeEvent(tradeDto)

        } catch (e: Exception) {
            log.error("Error processing message: {}", payload, e)
        }
    }
}
