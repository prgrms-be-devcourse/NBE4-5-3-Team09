package com.coing.infra.upbit.adapter.websocket.handler

import com.coing.infra.upbit.adapter.websocket.UpbitWebSocketEventAdapter
import com.coing.infra.upbit.adapter.websocket.dto.UpbitWebSocketOrderbookDto
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

/**
 * Upbit WebSocket Orderbook Request Handler
 *
 *
 * Upbit WebSocket 에 Subscription 메시지를 전송하고 Simple Format 형식의 Orderbook(호가) 데이터를 수신합니다.
 * 수신된 메시지를 JSON으로 파싱하고 OrderbookDto로 매핑한 후 데이터를 외부로 내보냅니다.
 */
@Component
class UpbitWebSocketOrderbookHandler(
    private val upbitWebSocketEventAdapter: UpbitWebSocketEventAdapter,
    private val upbitRequestBuilder: UpbitRequestBuilder,
    private val objectMapper: ObjectMapper = ObjectMapper(),
) : BinaryWebSocketHandler() {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * 연결 수립 후 초기 구독 메시지 전송
     * @param session
     * @throws Exception
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("Upbit WebSocket Orderbook connection established.")
        val subscribeMessage = upbitRequestBuilder.makeWebSocketRequest(EnumUpbitWebSocketRequestType.ORDERBOOK)
        log.info("Sending subscription message: {}", subscribeMessage)
        session.sendMessage(TextMessage(subscribeMessage))
    }

    /**
     * 바이너리 혹은 텍스트 메시지를 수신하여 Orderbook DTO로 변환
     * UpbitDataService와 연동해 수신된 Orderbook 데이터를 저장/처리
     * @param session
     * @param message
     * @throws Exception
     */
    public override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val payload = String(message.payload.array(), StandardCharsets.UTF_8)
        if (payload.isNotEmpty()) processMessage(payload)
    }

    /**
     * 수신한 메시지를 파싱하여 각 타입별로 UpbitDataService로 처리 요청합니다.
     * @param payload JSON 형식의 메시지
     */
    private fun processMessage(payload: String) {
        try {
            // keepalive 메시지인 경우 무시
            if ("{\"status\":\"UP\"}" == payload) {
                log.debug("Received keepalive message: {}", payload)
                return
            }
            val orderbookDto = objectMapper.readValue(payload, UpbitWebSocketOrderbookDto::class.java)
            upbitWebSocketEventAdapter.handleOrderbookEvent(orderbookDto)
        } catch (e: Exception) {
            log.error("Error processing message: {}", payload, e)
        }
    }
}
