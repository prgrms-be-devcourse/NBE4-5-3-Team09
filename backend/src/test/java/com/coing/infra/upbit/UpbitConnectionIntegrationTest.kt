package com.coing.infra.upbit

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest
class UpbitConnectionIntegrationTest {
    @Autowired
    private lateinit var webSocketClient: WebSocketClient

    @Value("\${upbit.websocket.uri}")
    private lateinit var upbitWebSocketUri: String

    @Test
    @DisplayName("Upbit Connection 연결 성공")
    fun successUpbitWebSocketConnectionPing() {
        // Given
        val latch = CountDownLatch(1)
        val testHandler: WebSocketHandler = object : AbstractWebSocketHandler() {
            override fun afterConnectionEstablished(session: WebSocketSession) {
                session.sendMessage(PingMessage(ByteBuffer.wrap("PING".toByteArray(StandardCharsets.UTF_8))))
            }

            public override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
                val payload = String(message.payload.array(), StandardCharsets.UTF_8)
                if ("PING" == payload) {
                    latch.countDown()
                }
            }
        }

        // when
        webSocketClient.execute(testHandler, upbitWebSocketUri)

        // then: 최대 5초 동안 Pong 응답(PING)이 도착하는지 확인
        val connected = latch.await(10, TimeUnit.SECONDS)
        Assertions.assertThat(connected).isTrue()
    }
}
