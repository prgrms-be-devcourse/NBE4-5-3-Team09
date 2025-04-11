package com.coing.domain.coin.common.event

import com.coing.domain.coin.common.dto.FallbackEvent
import com.coing.domain.coin.orderbook.service.OrderbookService
import com.coing.domain.coin.ticker.service.TickerService
import com.coing.domain.coin.trade.service.TradeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FallbackEventListenerTest {

    private lateinit var listener: FallbackEventListener

    private lateinit var orderbookService: OrderbookService
    private lateinit var tickerService: TickerService
    private lateinit var tradeService: TradeService

    @BeforeEach
    fun setUp() {
        orderbookService = mock(OrderbookService::class.java)
        tickerService = mock(TickerService::class.java)
        tradeService = mock(TradeService::class.java)

        listener = FallbackEventListener(orderbookService, tickerService, tradeService)
    }

    @Test
    @DisplayName("ORDERBOOK fallback 이벤트 처리")
    fun handleOrderbookFallback() {
        val event = FallbackEvent("ORDERBOOK", "13:05:22")

        listener.onFallbackEvent(event)

        verify(orderbookService, times(1)).fallbackUpdate("13:05:22")
        verifyNoInteractions(tickerService, tradeService)
    }

    @Test
    @DisplayName("TICKER fallback 이벤트 처리")
    fun handleTickerFallback() {
        val event = FallbackEvent("TICKER", "13:05:22")

        listener.onFallbackEvent(event)

        verify(tickerService, times(1)).fallbackUpdate("13:05:22")
        verifyNoInteractions(orderbookService, tradeService)
    }

    @Test
    @DisplayName("TRADE fallback 이벤트 처리")
    fun handleTradeFallback() {
        val event = FallbackEvent("TRADE", "13:05:22")

        listener.onFallbackEvent(event)

        verify(tradeService, times(1)).fallbackUpdate("13:05:22")
        verifyNoInteractions(orderbookService, tickerService)
    }

    @Test
    @DisplayName("정의되지 않은 도메인 처리")
    fun handleUnknownFallback() {
        val event = FallbackEvent("UNKNOWN", "13:05:22")

        listener.onFallbackEvent(event)

        verifyNoInteractions(orderbookService, tickerService, tradeService)
    }
}