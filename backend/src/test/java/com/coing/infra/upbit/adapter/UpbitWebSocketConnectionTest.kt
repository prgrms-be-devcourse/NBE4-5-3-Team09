package com.coing.infra.upbit.adapter

import com.coing.infra.upbit.adapter.websocket.UpbitWebSocketConnection
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketHandler
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class UpbitWebSocketConnectionTest {

    private val webSocketClient: WebSocketClient = mock()
    private val handler: UpbitWebSocketHandler = mock()
    private val session: WebSocketSession = mock()

    private lateinit var connection: UpbitWebSocketConnection

    private val uri = "wss://api.upbit.com/websocket/v1"

    @BeforeEach
    fun setUp() {
        connection = UpbitWebSocketConnection(
            webSocketClient,
            handler,
            uri,
            "TEST"
        )
    }

    @Test
    @DisplayName("WebSocket 연결 성공")
    fun connectionSuccess() {
        // given : webSocketClient.execute()가 성공하는 CompletableFuture 반환
        whenever(webSocketClient.execute(handler, uri))
            .thenReturn(CompletableFuture.completedFuture(session))

        // when
        connection.connect()

        // then: 연결 성공 시 isConnected() == true
        await().atMost(500, TimeUnit.MILLISECONDS).untilAsserted {
            assertTrue(connection.isConnected)
        }
    }

    @Test
    @DisplayName("WebSocket 연결 실패 - CompletableFuture 예외")
    fun connectionFailure() {
        // given : webSocketClient.execute()가 실패하는 CompletableFuture 반환
        val future = CompletableFuture<WebSocketSession>()
        future.completeExceptionally(RuntimeException("Connection Error."))
        whenever(webSocketClient.execute(handler, uri)).thenReturn(future)

        // when
        connection.connect()

        // then: 연결 실패 시 isConnected() == false
        await().atMost(500, TimeUnit.MILLISECONDS).untilAsserted {
            assertFalse(connection.isConnected)
        }
    }

    @Test
    @DisplayName("WebSocket 연결 실패 - 애러 발생")
    fun connectionFailure2() {
        // given : webSocketClient.execute() 호출 시 예외 발생
        whenever(webSocketClient.execute(handler, uri))
            .thenThrow(RuntimeException("Connection Error."))

        // when
        connection.connect()

        // Then: 즉시 isConnected = false & call scheduleReconnect()
        assertFalse(connection.isConnected)
    }

//	@Test
//	@DisplayName("PING 전송 성공")
//	public void sendPingMessageWhenConnected() throws IOException {
//		// given
//		when(session.isOpen()).thenReturn(true);
//		ReflectionTestUtils.setField(connection, "session", session);
//		ReflectionTestUtils.setField(connection, "isConnected", true);
//
//		// when
//		connection.sendPing();
//
//		// then : "PING" 메시지가 전송되었는지 검증
//		ArgumentCaptor<PingMessage> messageCaptor = ArgumentCaptor.forClass(PingMessage.class);
//		verify(session, times(1)).sendMessage(messageCaptor.capture());
//	}
//
//	@Test
//	@DisplayName("PING 전송 실패 - 연결되지 않은 경우")
//	public void sendPingWhenNotConnected() {
//		// given: 연결되지 않은 상태
//		ReflectionTestUtils.setField(connection, "isConnected", false);
//		UpbitWebSocketConnection spyConnection = spy(connection);
//
//		// when
//		spyConnection.sendPing();
//
//		// then: 재연결 스케줄링이 호출되었는지 검증
//		verify(spyConnection, atLeastOnce()).scheduleReconnect();
//	}
}
