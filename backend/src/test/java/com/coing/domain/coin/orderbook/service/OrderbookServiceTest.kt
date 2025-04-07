package com.coing.domain.coin.orderbook.service

import com.coing.domain.coin.orderbook.dto.OrderbookDto
import com.coing.domain.coin.orderbook.entity.Orderbook
import com.coing.domain.coin.orderbook.entity.OrderbookUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.messaging.simp.SimpMessageSendingOperations

class OrderbookServiceTest {
    private val messagingTemplate: SimpMessageSendingOperations = mock()
    private lateinit var orderbookService: OrderbookService
    private lateinit var testOrderbook: Orderbook

    @BeforeEach
    fun setUp() {
        orderbookService = OrderbookService(messagingTemplate)
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
    @DisplayName("publish 성공")
    fun publish() {
        // given
        val dto = OrderbookDto.from(testOrderbook)

        // when
        orderbookService.publish(dto)

        // then
        val expectedChannel = "/sub/coin/orderbook/${testOrderbook.code}"
        argumentCaptor<OrderbookDto>().apply {
            verify(messagingTemplate, times(1)).convertAndSend(eq(expectedChannel), capture())
            val sentDto = firstValue

            assertEquals("orderbook", sentDto.type)
            assertEquals("KRW-BTC", sentDto.code)
        }
    }
}
