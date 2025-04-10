package com.coing.infra.upbit.adapter

import com.coing.infra.upbit.adapter.websocket.UpbitWebSocketConnection
import com.coing.infra.upbit.adapter.websocket.UpbitWebSocketManager
import com.coing.infra.upbit.adapter.websocket.enums.EnumUpbitWebSocketType
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketOrderbookHandler
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketTickerHandler
import com.coing.infra.upbit.adapter.websocket.handler.UpbitWebSocketTradeHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.socket.client.WebSocketClient

@ExtendWith(MockitoExtension::class)
class UpbitWebSocketManagerTest {

    @Mock
    private lateinit var webSocketClient: WebSocketClient

    @Mock
    private lateinit var orderbookHandler: UpbitWebSocketOrderbookHandler

    @Mock
    private lateinit var tickerHandler: UpbitWebSocketTickerHandler

    @Mock
    private lateinit var tradeHandler: UpbitWebSocketTradeHandler

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var service: UpbitWebSocketManager

    private val uri = "wss://api.upbit.com/websocket/v1"

    @BeforeEach
    fun setUp() {
        service = UpbitWebSocketManager(
            webSocketClient,
            orderbookHandler,
            tickerHandler,
            tradeHandler,
            eventPublisher
        )
        ReflectionTestUtils.setField(service, "upbitWebSocketUri", uri)
    }

    @Test
    @DisplayName("init() 시 각 타입별 Connection 생성")
    fun initSuccess() {
        // when
        service.init()

        // then
        val connections = getConnections()

        Assertions.assertEquals(3, connections.size)
    }

    private fun getConnections(): Map<EnumUpbitWebSocketType, UpbitWebSocketConnection> {
        @Suppress("UNCHECKED_CAST")
        return ReflectionTestUtils.getField(service, "connections") as Map<EnumUpbitWebSocketType, UpbitWebSocketConnection>
    }

//	@Test
//	@DisplayName("sendPingMessages() 호출 시 모든 Connection에 Ping 전송")
//	public void sendPingMessages() {
//		// given
//		service.init();
//
//		@SuppressWarnings("unchecked")
//		Map<EnumUpbitWebSocketType, UpbitWebSocketConnection> connections =
//			(Map<EnumUpbitWebSocketType, UpbitWebSocketConnection>)
//				ReflectionTestUtils.getField(service, "connections");
//
//		connections.forEach((type, conn) -> {
//			UpbitWebSocketConnection spyConn = spy(conn);
//			connections.put(type, spyConn);
//		});
//
//		// when
//		service.sendPingMessages();
//
//		// then
//		connections.forEach((type, spyConn) -> verify(spyConn, times(1)).sendPing());
//	}
//
//	@Test
//	@DisplayName("disconnectAll() 시 모든 Connection이 종료")
//	void testDisconnectAll() {
//		// given
//		service.init();
//
//		@SuppressWarnings("unchecked")
//		Map<EnumUpbitWebSocketType, UpbitWebSocketConnection> connections =
//			(Map<EnumUpbitWebSocketType, UpbitWebSocketConnection>)
//				ReflectionTestUtils.getField(service, "connections");
//
//		connections.forEach((type, conn) -> {
//			UpbitWebSocketConnection spyConn = spy(conn);
//			connections.put(type, spyConn);
//		});
//
//		// when
//		service.disconnectAll();
//
//		// then
//		connections.forEach((type, spyConn) -> verify(spyConn, times(1)).disconnect());
//	}
}
