package com.coing.infra.upbit.handler

import com.coing.infra.upbit.adapter.UpbitDataService
import com.coing.infra.upbit.dto.UpbitWebSocketTickerDto
import com.coing.infra.upbit.enums.EnumUpbitRequestType
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
    private val upbitDataService: UpbitDataService,
    private val upbitRequestBuilder: UpbitRequestBuilder
) : BinaryWebSocketHandler() {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("Upbit WebSocket Ticker connection established.")
        val subscribeMessage = upbitRequestBuilder.makeRequest(EnumUpbitRequestType.TICKER)
        session.sendMessage(TextMessage(subscribeMessage))
    }

    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val payload = String(message.payload.array(), StandardCharsets.UTF_8)
        if (payload.isNotEmpty()) processMessage(payload)
    }

    private fun processMessage(payload: String) {
        try {
            if (payload == """{"status":"UP"}""") {
                log.debug("Received keepalive message: {}", payload)
                return
            }

            val tickerDto = objectMapper.readValue(payload, UpbitWebSocketTickerDto::class.java)
            upbitDataService.processTickerData(tickerDto)
        } catch (e: Exception) {
            log.error("Error processing ticker message: {}", payload, e)
        }
    }
}
