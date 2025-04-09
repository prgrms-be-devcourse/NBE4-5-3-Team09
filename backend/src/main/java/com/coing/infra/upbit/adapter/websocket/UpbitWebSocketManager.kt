package com.coing.infra.upbit.adapter.websocket

import com.coing.infra.upbit.adapter.websocket.enums.EnumUpbitWebSocketType
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketHandler
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketOrderbookHandler
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketTickerHandler
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketTradeHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketClient

/**
 * 여러 WebSocket Type별로 UpbitWebSocketConnection을 생성하고 관리하는 서비스
 */
@Component
class UpbitWebSocketManager(
    private val webSocketClient: WebSocketClient,
    private val orderbookHandler: UpbitWebSocketOrderbookHandler,
    private val tickerHandler: UpbitWebSocketTickerHandler,
    private val tradeHandler: UpbitWebSocketTradeHandler,
) {
    private val connections = mutableMapOf<EnumUpbitWebSocketType, UpbitWebSocketConnection>()

    @Value("\${upbit.websocket.uri}")
    private lateinit var upbitWebSocketUri: String

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * 애플리케이션 시작 후 연결
     */
    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        // ORDERBOOK
        val orderbookComposite = UpbitWebSocketHandler(listOf(orderbookHandler))
        val orderbookConn = UpbitWebSocketConnection(webSocketClient, orderbookComposite, upbitWebSocketUri, "ORDERBOOK")
        connections[EnumUpbitWebSocketType.ORDERBOOK] = orderbookConn
        orderbookConn.connect()

        // TICKER
        val tickerComposite = UpbitWebSocketHandler(listOf(tickerHandler))
        val tickerConn = UpbitWebSocketConnection(webSocketClient, tickerComposite, upbitWebSocketUri, "TICKER")
        connections[EnumUpbitWebSocketType.TICKER] = tickerConn
        tickerConn.connect()

        // TRADE
        val tradeComposite = UpbitWebSocketHandler(listOf(tradeHandler))
        val tradeConn = UpbitWebSocketConnection(webSocketClient, tradeComposite, upbitWebSocketUri, "TRADE")
        connections[EnumUpbitWebSocketType.TRADE] = tradeConn
        tradeConn.connect()
    }

    /**
     * 60초마다 PING 메시지를 전송하여 WebSocket 연결을 유지합니다.
     * 연결이 되어 있지 않은 경우 재연결을 시도합니다.
     */
    @Scheduled(fixedRate = 60000)
    fun sendPingMessages() {
        connections.values.forEach { it.sendPing() }
    }

    fun disconnectAll() {
        connections.values.forEach { it.disconnect() }
    }

    fun getConnection(type: EnumUpbitWebSocketType) = connections[type]
}
