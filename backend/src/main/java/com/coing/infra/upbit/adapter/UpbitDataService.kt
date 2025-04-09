package com.coing.infra.upbit.adapter

import com.coing.domain.coin.common.port.CoinDataHandler
import com.coing.domain.coin.orderbook.entity.Orderbook
import com.coing.domain.coin.ticker.entity.Ticker
import com.coing.domain.coin.trade.entity.Trade
import com.coing.infra.upbit.dto.UpbitWebSocketOrderbookDto
import com.coing.infra.upbit.dto.UpbitWebSocketTickerDto
import com.coing.infra.upbit.dto.UpbitWebSocketTradeDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Upbit WebSocket 수신 데이터를 처리하고 관리하는 서비스 계층
 *
 *
 * 데이터를 가공 및 캐싱하여 데이터베이스에 저장하기 위한 비즈니스 로직 담당
 */
@Service
class UpbitDataService(
    private val orderbookCoinDataHandler: CoinDataHandler<Orderbook>,
    private val tickerCoinDataHandler: CoinDataHandler<Ticker>,
    private val tradeCoinDataHandler: CoinDataHandler<Trade>,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun handleOrderbookEvent(dto: UpbitWebSocketOrderbookDto) {
        val orderbook = dto.toEntity()
        orderbookCoinDataHandler.update(orderbook)
    }

    fun handleTickerEvent(dto: UpbitWebSocketTickerDto) {
        // double oneMinuteRate = tickerService.calculateOneMinuteRate(dto.getCode(), dto.getTradePrice());
        val ticker: Ticker = dto.toEntity()
        tickerCoinDataHandler.update(ticker)
    }

    fun handleTradeEvent(dto: UpbitWebSocketTradeDto) {
        val trade = dto.toEntity()
        tradeCoinDataHandler.update(trade)
    }
}
