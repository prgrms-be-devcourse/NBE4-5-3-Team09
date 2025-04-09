package com.coing.infra.upbit.adapter.websocket.handler

import com.coing.infra.upbit.adapter.websocket.UpbitWebSocketEventAdapter
import com.coing.infra.upbit.adapter.websocket.dto.UpbitWebSocketTickerDto
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
class UpbitWebSocketTickerHandler(
    private val objectMapper: ObjectMapper,
    private val upbitDataService: UpbitWebSocketEventAdapter,
    private val upbitRequestBuilder: UpbitRequestBuilder
) : BinaryWebSocketHandler() {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("Upbit WebSocket Ticker connection established.")
        val subscribeMessage = upbitRequestBuilder.makeWebSocketRequest(EnumUpbitWebSocketRequestType.TICKER)
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

            val tickerDto = objectMapper.readValue(payload, UpbitWebSocketTickerDto::class.java)
            upbitDataService.handleTickerEvent(tickerDto)
        } catch (e: Exception) {
            log.error("Error processing ticker message: {}", payload, e)
        }
    }
}
