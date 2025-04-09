package com.coing.infra.upbit.adapter.websocket

import com.coing.domain.coin.common.port.CoinDataHandler
import com.coing.domain.coin.orderbook.entity.Orderbook
import com.coing.domain.coin.ticker.entity.Ticker
import com.coing.domain.coin.ticker.service.TickerService
import com.coing.domain.coin.trade.entity.Trade
import com.coing.infra.upbit.adapter.websocket.dto.UpbitWebSocketOrderbookDto
import com.coing.infra.upbit.adapter.websocket.dto.UpbitWebSocketTickerDto
import com.coing.infra.upbit.adapter.websocket.dto.UpbitWebSocketTradeDto
import org.springframework.stereotype.Component

/**
 * Upbit WebSocket 수신 데이터를 처리하고 관리하는 어댑터
 *
 *
 * 데이터를 가공 및 캐싱하여 데이터베이스에 저장하기 위한 비즈니스 로직 담당
 */
@Component
class UpbitWebSocketEventAdapter(
    private val orderbookHandler: CoinDataHandler<Orderbook>,
    private val ticketHandler: CoinDataHandler<Ticker>,
    private val tradeHandler: CoinDataHandler<Trade>,
    private val tickerService: TickerService
) {
    fun handleOrderbookEvent(dto: UpbitWebSocketOrderbookDto) {
        val orderbook = dto.toEntity()
        orderbookHandler.update(orderbook)
    }

    fun handleTickerEvent(dto: UpbitWebSocketTickerDto) {
        val oneMinuteRate = tickerService.calculateOneMinuteRate(dto.code, dto.tradePrice)
        val ticker: Ticker = dto.toEntity(oneMinuteRate)
        ticketHandler.update(ticker)
    }

    fun handleTradeEvent(dto: UpbitWebSocketTradeDto) {
        val trade = dto.toEntity()
        tradeHandler.update(trade)
    }
}
