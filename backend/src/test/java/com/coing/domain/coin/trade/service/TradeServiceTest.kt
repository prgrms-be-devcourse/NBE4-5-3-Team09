package com.coing.domain.coin.trade.service

import com.coing.domain.coin.common.enums.AskBid.ASK
import com.coing.domain.coin.common.enums.Change.EVEN
import com.coing.domain.coin.trade.dto.TradeDto
import com.coing.domain.coin.trade.entity.Trade
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.SimpMessageSendingOperations
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class TradeServiceTest {

    @Mock
    lateinit var simpMessageSendingOperations: SimpMessageSendingOperations

    @Mock
    lateinit var messageUtil: MessageUtil

    @InjectMocks
    lateinit var tradeService: TradeService

    private lateinit var trade: Trade

    @BeforeEach
    fun setUp() {
        // 테스트용 Trade 객체 생성
        trade = Trade(
            type = "trade",
            code = "KRW-BTC",
            tradePrice = 1000.0,
            tradeVolume = 0.5,
            askBid = ASK,
            prevClosingPrice = 0.0,
            change = EVEN,
            changePrice = 0.0,
            tradeDate = LocalDate.now(),
            tradeTime = LocalTime.now(),
            tradeTimeStamp = 0L,
            timestamp = 0L,
            sequentialId = 0L,
            bestAskPrice = 0.0,
            bestAskSize = 0.0,
            bestBidPrice = 0.0,
            bestBidSize = 0.0
        )
    }

    @Test
    @DisplayName("getTrades 성공 - 존재하는 체결 목록 조회")
    fun getTrades_Success() {
        // given
        tradeService.updateTrade(trade)

        // when
        val result = tradeService.getTrades(trade.code)

        // then
        assertNotNull(result)
        assertFalse(result.isEmpty())
        assertEquals(trade.code, result[0].code)
    }

    @Test
    @DisplayName("getTrades 실패 - 존재하지 않는 체결 목록 조회 시 예외 발생")
    fun getTrades_Failure() {
        // given
        `when`(messageUtil.resolveMessage("trade.not.found"))
            .thenReturn("해당 체결을 찾을 수 없습니다.")

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            tradeService.getTrades("KRW-ETH")
        }

        assertEquals("해당 체결을 찾을 수 없습니다.", exception.message)
    }

    @Test
    fun testPublish() {
        // given
        val dto = TradeDto.of(trade, 0.0, 0.0, 0.0)

        // when
        tradeService.publish(dto)

        // then
        verify(simpMessageSendingOperations, times(1))
            .convertAndSend(eq("/sub/coin/trade/KRW-BTC"), any(TradeDto::class.java))
    }
}
