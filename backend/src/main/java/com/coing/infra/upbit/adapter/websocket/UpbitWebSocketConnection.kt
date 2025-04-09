package com.coing.infra.upbit.adapter.websocket

import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.Volatile

/**
 * 하나의 WebSocket 연결을 관리하는 클래스
 */
class UpbitWebSocketConnection(
    private val webSocketClient: WebSocketClient,
    private val handler: UpbitWebSocketHandler,
    private val webSocketUri: String,
    // ex. "ORDERBOOK", "TRADE"
    private val name: String
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

    // 재연결 관련
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val isReconnecting = AtomicBoolean(false)
    private var reconnectAttempts = 0L

    private val BASE_DELAY_SECONDS = 2L
    private val MAX_DELAY_SECONDS = 60L


    /**
     * WebSocket 연결 시도
     *
     *
     * 비동기적으로 연결 결과를 처리하며, 성공 시 session을 저장하고 재연결 시도 횟수를 초기화합니다.
     * 연결 실패 또는 예외 발생 시, 재연결 시도
     */
    @Synchronized
    fun connect() {
        try {
            // 비동기로 WebSocket 연결을 실행
            val future = webSocketClient.execute(handler, webSocketUri)
            future.whenComplete { webSocketSession, throwable ->
                if (throwable == null) {
                    session = webSocketSession
                    _isConnected = true
                    reconnectAttempts = 0
                    log.info("[$name] WebSocket connected")
                } else {
                    _isConnected = false
                    log.error("[$name] WebSocket connection failed: ${throwable.message}", throwable)
                    scheduleReconnect()
                }
            }
        } catch (e: Exception) {
            _isConnected = false
            log.error("[$name] Exception during WebSocket connection: ${e.message}", e)
            scheduleReconnect()
        }
    }

    /**
     * 지수 백오프 기반 재연결
     *
     *
     * 연결 실패 시 BASE_DELAY_SECONDS에 2^(reconnectAttempts)를 곱한 지연 후 재연결을 시도하며,
     * 최대 MAX_DELAY_SECONDS까지 지연 시간을 늘립니다.
     */
    private fun scheduleReconnect() {
        if (isReconnecting.compareAndSet(false, true)) {
            val delay = minOf(MAX_DELAY_SECONDS, BASE_DELAY_SECONDS * (1L shl reconnectAttempts.toInt()))
            log.info("[$name] Scheduling reconnection attempt in $delay seconds")

            scheduler.schedule({
                log.info("[$name] Attempting reconnect...")
                connect()
                reconnectAttempts++
                isReconnecting.set(false)
            }, delay, TimeUnit.SECONDS)
        }
    }

    /**
     * Ping 메시지 전송
     */
    fun sendPing() {
        if (isConnected && session?.isOpen == true) {
            try {
                session?.sendMessage(PingMessage())
                log.info("[$name] Sent PING")
            } catch (e: Exception) {
                log.error("[$name] Failed to send PING: ${e.message}", e)
            }
        } else {
            log.warn("[$name] Session not connected. Scheduling reconnect...")
            scheduleReconnect()
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
                log.info("[$name] WebSocket session closed.")
            }
        } catch (e: Exception) {
            log.error("[$name] Error closing session: ${e.message}", e)
        }
        _isConnected = false
    }
}
