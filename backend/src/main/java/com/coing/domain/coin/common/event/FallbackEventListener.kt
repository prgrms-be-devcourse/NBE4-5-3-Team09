package com.coing.domain.coin.common.event

import com.coing.domain.coin.common.dto.FallbackEvent
import com.coing.domain.coin.orderbook.service.OrderbookService
import com.coing.domain.coin.ticker.service.TickerService
import com.coing.domain.coin.trade.service.TradeService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class FallbackEventListener(
    private val orderbookService: OrderbookService,
    private val tickerService: TickerService,
    private val tradeService: TradeService
) {
    @EventListener
    fun onFallbackEvent(event: FallbackEvent) {
        when (event.domain) {
            "ORDERBOOK" -> orderbookService.fallbackUpdate(event.lastUpdate)
            "TICKER" -> tickerService.fallbackUpdate(event.lastUpdate)
            "TRADE" -> tradeService.fallbackUpdate(event.lastUpdate)
        }
    }
}