package com.coing.domain.coin.orderbook.service

import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.orderbook.dto.OrderbookDto
import com.coing.domain.coin.orderbook.entity.Orderbook
import com.coing.domain.coin.orderbook.entity.OrderbookUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class OrderbookServiceTest {
    private val orderbookPublisher: EventPublisher<OrderbookDto> = mock()
    private lateinit var orderbookService: OrderbookService
    private lateinit var testOrderbook: Orderbook

    @BeforeEach
    fun setUp() {
        orderbookService = OrderbookService(orderbookPublisher)
        val unit = OrderbookUnit(
            askPrice = 100.0,
            bidPrice = 90.0,
            askSize = 10.0,
            bidSize = 5.0
        )

        testOrderbook = Orderbook(
            type = "orderbook",
            code = "KRW-BTC",
            totalAskSize = 10.0,
            totalBidSize = 5.0,
            orderbookUnits = listOf(unit),
            timestamp = System.currentTimeMillis(),
            level = 0.0
        )
    }

    @Test
    @DisplayName("update 성공")
    fun update() {
        // given
        val captor = argumentCaptor<OrderbookDto>()

        // when
        orderbookService.update(testOrderbook)

        // then
        verify(orderbookPublisher, times(1))
            .publish(captor.capture())
        assertEquals("KRW-BTC", captor.firstValue.code)
    }

    @Test
    @DisplayName("publish 성공")
    fun publish() {
        // given
        val dto = OrderbookDto.from(testOrderbook)

        // when
        orderbookService.publish(dto)

        // then
        val expectedChannel = "/sub/coin/orderbook/${testOrderbook.code}"
        argumentCaptor<OrderbookDto>().apply {
            verify(orderbookPublisher, times(1)).publish(capture())
            val sentDto = firstValue

            assertEquals("orderbook", sentDto.type)
            assertEquals("KRW-BTC", sentDto.code)
        }
    }
}
