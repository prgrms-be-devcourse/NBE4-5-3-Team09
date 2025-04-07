package com.coing.infra.upbit.handler

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

@ExtendWith(MockitoExtension::class)
class UpbitWebSocketHandlerTest {
    private val session: WebSocketSession = mock()
    private val handler1: BinaryWebSocketHandler = mock()
    private val handler2: BinaryWebSocketHandler = mock()

    private lateinit var handler: UpbitWebSocketHandler

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // given
        handler = UpbitWebSocketHandler(listOf(handler1, handler2))
    }

    @Test
    @DisplayName("afterConnectionEstablished() 성공")
    fun successAfterConnectionEstablished() {
        // when
        handler.afterConnectionEstablished(session)
        // then
        verify(handler1, times(1)).afterConnectionEstablished(session)
        verify(handler2, times(1)).afterConnectionEstablished(session)
    }

    @Test
    @DisplayName("handleMessage() 성공")
    @Throws(Exception::class)
    fun successHandleMessage() {
        // when
        val message = BinaryMessage(ByteBuffer.wrap("test message".toByteArray(StandardCharsets.UTF_8)))
        handler.handleMessage(session, message)
        // then
        verify(handler1, times(1)).handleMessage(session, message)
        verify(handler2, times(1)).handleMessage(session, message)
    }

    @Test
    @DisplayName("handleMessage() 일부 성공 - 일부 handler 예외 발생 시")
    @Throws(
        Exception::class
    )
    fun handleMessageExceptionInOneHandler() {
        // given: handler1에서 예외 발생
        val message = BinaryMessage(ByteBuffer.wrap("test message".toByteArray(StandardCharsets.UTF_8)))
        doThrow(RuntimeException("Handler error")).whenever(handler1).handleMessage(session, message)

        // when
        handler.handleMessage(session, message)

        // then: 모든 handler 정상 호출
        verify(handler1, times(1)).handleMessage(session, message)
        verify(handler2, times(1)).handleMessage(session, message)
    }

    @Test
    @DisplayName("handleTransportError() 성공")
    @Throws(Exception::class)
    fun successHandleTransportError() {
        // when
        val ex = Exception("Exception occurred")
        handler.handleTransportError(session, ex)

        // then
        verify(handler1, times(1)).handleTransportError(session, ex)
        verify(handler2, times(1)).handleTransportError(session, ex)
    }

    @Test
    @DisplayName("afterConnectionClosed() 성공")
    @Throws(Exception::class)
    fun successAfterConnectionClosed() {
        // when
        val status = CloseStatus.NORMAL
        handler.afterConnectionClosed(session, status)

        // then
        verify(handler1, times(1)).afterConnectionClosed(session, status)
        verify(handler2, times(1)).afterConnectionClosed(session, status)
    }
}
