package com.coing.infra.upbit.handler

import com.coing.infra.upbit.adapter.websocket.UpbitWebSocketEventAdapter
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketOrderbookHandler
import com.coing.infra.upbit.adapter.websocket.dto.UpbitWebSocketOrderbookDto
import com.coing.infra.upbit.util.UpbitRequestBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@ExtendWith(MockitoExtension::class)
class UpbitWebSocketOrderbookHandlerTest {
    private val upbitWebSocketEventAdapter: UpbitWebSocketEventAdapter = mock()
    private val session: WebSocketSession = mock()
    private val upbitRequestBuilder: UpbitRequestBuilder = mock()

    private lateinit var handler: UpbitWebSocketOrderbookHandler

    @BeforeEach
    fun setUp() {
        handler = UpbitWebSocketOrderbookHandler(upbitWebSocketEventAdapter, upbitRequestBuilder)
    }

    @Test
    @DisplayName("afterConnectionEstablished() 성공")
    fun successAfterConnectionEstablished() {
        // given
        whenever(upbitRequestBuilder.makeWebSocketRequest(any())).thenReturn("""[{"ticket":"orderbook"}]""")

        // when
        handler.afterConnectionEstablished(session)

        // then
        val captor = argumentCaptor<TextMessage>()
        verify(session).sendMessage(captor.capture())
        val sentMessage = captor.firstValue.payload
        assertThat(sentMessage).isNotEmpty()
        assertThat(sentMessage).contains("orderbook")
    }

    @Test
    @DisplayName("handleBinaryMessage() 성공")
    fun successHandleBinaryMessage() {
        // given
        val json = """
            {
              "ty": "orderbook",
              "cd": "KRW-BTC",
              "tms": 1704867306396,
              "tas": 7.3262086,
              "tbs": 29.27948667,
              "obu": [
                {
                  "ap": 61820000,
                  "bp": 61800000,
                  "as": 1.44125174,
                  "bs": 8.95463042
                }
              ],
              "st": "REALTIME",
              "lv": 10000
            }
        """.trimIndent()

        val binaryMessage = BinaryMessage(ByteBuffer.wrap(json.toByteArray(StandardCharsets.UTF_8)))

        // when
        val spyHandler = spy(handler)
        spyHandler.handleBinaryMessage(session, binaryMessage)

        // then
        verify(upbitWebSocketEventAdapter).handleOrderbookEvent(any<UpbitWebSocketOrderbookDto>())
    }

    @Test
    @DisplayName("handleBinaryMessage() Keepalive 메시지 무시")
    fun testHandleBinaryMessageIgnoresKeepalive() {
        // given: keepalive 메시지
        val keepalivePayload = "{\"status\":\"UP\"}"
         val binaryMessage = BinaryMessage(ByteBuffer.wrap(keepalivePayload.toByteArray(StandardCharsets.UTF_8)))

        // when
        handler.handleBinaryMessage(session, binaryMessage)

        // then
        verify(upbitWebSocketEventAdapter, never()).handleOrderbookEvent(any())
    }

    @Test
    @DisplayName("handleBinaryMessage() 실패 - Invalid Json Payload")
    fun failureHandleBinaryMessage() {
        // given: 유효하지 않은 JSON payload
        val invalidJson = "invalid json"
        val binaryMessage = BinaryMessage(ByteBuffer.wrap(invalidJson.toByteArray(StandardCharsets.UTF_8)))

        // when
        handler.handleBinaryMessage(session, binaryMessage)

        // then
        verify(upbitWebSocketEventAdapter, never()).handleOrderbookEvent(any())
    }
}
