package com.coing.infra.messaging.websocket

import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.orderbook.dto.OrderbookDto
import com.coing.domain.coin.ticker.dto.TickerDto
import com.coing.domain.coin.trade.dto.TradeDto
import com.coing.infra.messaging.websocket.constant.WebSocketSubscriptionTopics
import com.coing.infra.messaging.websocket.publisher.WebSocketEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.SimpMessageSendingOperations

@Configuration
class WebSocketEventPublisherConfig {
    @Bean
    fun tradeEventPublisher(messagingTemplate: SimpMessageSendingOperations): EventPublisher<TradeDto> {
        return WebSocketEventPublisher(messagingTemplate, WebSocketSubscriptionTopics.TRADES)
    }

    @Bean
    fun tickerEventPublisher(messagingTemplate: SimpMessageSendingOperations): EventPublisher<TickerDto> {
        return WebSocketEventPublisher(messagingTemplate, WebSocketSubscriptionTopics.TICKERS)
    }

    @Bean
    fun orderbookEventPublisher(messagingTemplate: SimpMessageSendingOperations): EventPublisher<OrderbookDto> {
        return WebSocketEventPublisher(messagingTemplate, WebSocketSubscriptionTopics.ORDERBOOKS)
    }
}