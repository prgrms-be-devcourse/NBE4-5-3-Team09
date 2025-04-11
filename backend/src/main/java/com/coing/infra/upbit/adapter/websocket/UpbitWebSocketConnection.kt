package com.coing.infra.upbit.adapter.websocket

import com.coing.domain.coin.common.dto.FallbackEvent
import com.coing.infra.upbit.adapter.websocket.enums.EnumUpbitWebSocketType
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketHandler
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.Volatile

/**
 * 하나의 WebSocket 연결을 관리하는 클래스
 */
class UpbitWebSocketConnection(
    private val webSocketClient: WebSocketClient,
    private val handler: UpbitWebSocketHandler,
    private val webSocketUri: String,
    // ex. "ORDERBOOK", "TRADE"
    private val domain: EnumUpbitWebSocketType,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * 현재 WebSocket 연결 상태를 반환합니다.
     */
    @Volatile
    private var _isConnected = false
    val isConnected: Boolean
        get() = _isConnected

    @Volatile
    private var session: WebSocketSession? = null

    /**
     * WebSocket 연결 시도
     *
     *
     * 비동기적으로 연결 결과를 처리하며, 성공 시 session을 저장하고 재연결 시도 횟수를 초기화합니다.
     * 연결 실패 또는 예외 발생 시, 재연결 시도
     */
    @Synchronized
    @Retry(name = "upbitWebSocket", fallbackMethod = "fallbackConnect") // 지수 백오프 기반 재연결
    fun connect() {
        try {
            // 비동기로 WebSocket 연결을 실행
            val future = webSocketClient.execute(handler, webSocketUri)
            session = future.get()  // 연결 성공 시 WebSocketSession 반환, 실패 시 예외 발생
            _isConnected = true
            log.info("[$domain] WebSocket connected.")
        } catch (e: Exception) {
            _isConnected = false
            log.error("[$domain] Exception during WebSocket connection: ${e.message}", e)
            throw e
        }
    }

    /**
     * fallbackConnect
     * 모든 재시도가 실패한 경우 호출되는 fallback 메서드입니다.
     * 여기서는 연결 장애 신호와 현재 시간(lastUpdate)을 웹소켓 이벤트 어댑터에 전달하여,
     * 기존 이벤트 전파 경로를 통해 해당 도메인의 서비스가 캐시된 데이터를 fallback 상태로 처리할 수 있도록 합니다.
     */
    fun fallbackConnect(ex: Throwable) {
        _isConnected = false
        val lastUpdate = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        log.info("[$domain] Triggering fallback event with last update time: $lastUpdate")
        eventPublisher.publishEvent(FallbackEvent(domain.name, lastUpdate))
    }

    /**
     * Ping 메시지 전송
     */
    fun sendPing() {
        if (isConnected && session?.isOpen == true) {
            try {
                session?.sendMessage(PingMessage())
                log.info("[$domain] Sent PING")
            } catch (e: Exception) {
                log.error("[$domain] Failed to send PING: ${e.message}", e)
                connect()
            }
        } else {
            log.warn("[$domain] Session not connected. Scheduling reconnect...")
            connect()
        }
    }

    /**
     * 명시적으로 연결 종료
     */
    @Synchronized
    fun disconnect() {
        try {
            if (session?.isOpen == true) {
                session?.close()
                log.info("[$domain] WebSocket session closed.")
            }
        } catch (e: Exception) {
            log.error("[$domain] Error closing session: ${e.message}", e)
        }
        _isConnected = false
    }
}
