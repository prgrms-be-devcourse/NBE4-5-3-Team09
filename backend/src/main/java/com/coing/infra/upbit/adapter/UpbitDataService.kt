package com.coing.infra.upbit.adapter

import com.coing.domain.coin.common.port.DataHandler
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
    private val orderbookDataHandler: DataHandler<Orderbook>,
    private val tickerDataHandler: DataHandler<Ticker>,
    private val tradeDataHandler: DataHandler<Trade>,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun handleOrderbookEvent(dto: UpbitWebSocketOrderbookDto) {
        val orderbook = dto.toEntity()
        orderbookDataHandler.update(orderbook)
    }

    fun handleTickerEvent(dto: UpbitWebSocketTickerDto) {
        // double oneMinuteRate = tickerService.calculateOneMinuteRate(dto.getCode(), dto.getTradePrice());
        val ticker: Ticker = dto.toEntity()
        tickerDataHandler.update(ticker)
    }

    fun handleTradeEvent(dto: UpbitWebSocketTradeDto) {
        val trade = dto.toEntity()
        tradeDataHandler.update(trade)
    }
}
