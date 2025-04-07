package com.coing.domain.coin.orderbook.entity

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class OrderbookTest {

    @Test
    @DisplayName("Orderbook Indicators 검증")
    fun calculateOrderbookIndicators() {
        // given
        val unit = OrderbookUnit(100.0, 90.0, 10.0, 5.0)
        val unit2 = OrderbookUnit(90.0, 100.0, 11.0, 7.0)
        val units: MutableList<OrderbookUnit> = LinkedList()
        units.add(unit)
        units.add(unit2)

        val orderbook = Orderbook(
            type = "orderbook",
            code = "KRW-BTC",
            totalAskSize = 10.0,
            totalBidSize = 5.0,
            orderbookUnits = units.toMutableList(),
            timestamp = System.currentTimeMillis(),
            level = 0.0
        )

        // then
        Assertions.assertEquals(100.0, orderbook.bestPrices.bestAskPrice)
        Assertions.assertEquals(90.0, orderbook.bestPrices.bestBidPrice)
        Assertions.assertEquals(10.0, orderbook.spread) // spread = 100 - 90 = 10
        Assertions.assertEquals(
            -0.3333333333, orderbook.imbalance,
            0.0001
        ) //imbalance = (bid - ask) / (bid+ask) = (5 - 10) / 15 = -0.3333
        Assertions.assertEquals(95.0, orderbook.midPrice) // midPrice = (100 + 90) / 2 = 95
        Assertions.assertEquals(0.0, orderbook.liquidityDepth)
    }
}
