package com.coing.infra.upbit.adapter.websocket.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler

/**
 * Upbit WebSocket Composite Handler
 *
 *
 * 여러 개의 개별 BinaryWebSocketHandler를 리스트로 받아, 하나의 WebSocket 연결에서 모든 이벤트를 순차적으로 처리합니다.
 * 개별 Handler에서 예외가 발생하더라도 다른 Handler가 정상적으로 동작하도록 개별 try-catch로 처리합니다.
 */
@Component
class UpbitWebSocketHandler(
    private val handlers: List<BinaryWebSocketHandler> = emptyList()
) : BinaryWebSocketHandler() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        handlers.forEach { handler ->
            try {
                handler.afterConnectionEstablished(session)
            } catch (e: Exception) {
                log.error(
                    "Error in handler {} after connection established: {}",
                    handler.javaClass.simpleName, e.message, e
                )
            }
        }
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        handlers.forEach { handler ->
            try {
                handler.handleMessage(session, message)
            } catch (e: Exception) {
                log.error(
                    "Error in handler {} during message handling: {}",
                    handler.javaClass.simpleName, e.message, e
                )
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        handlers.forEach { handler ->
            try {
                handler.handleTransportError(session, exception)
            } catch (e: Exception) {
                log.error(
                    "Error in handler {} during transport error handling: {}",
                    handler.javaClass.simpleName, e.message, e
                )
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        handlers.forEach { handler ->
            try {
                handler.afterConnectionClosed(session, closeStatus)
            } catch (e: Exception) {
                log.error(
                    "Error in handler {} after connection closed: {}",
                    handler.javaClass.simpleName, e.message, e
                )
            }
        }
    }
}
