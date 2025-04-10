package com.coing.domain.coin.trade.service

import com.coing.domain.coin.common.enums.AskBid.ASK
import com.coing.domain.coin.common.enums.Change.EVEN
import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.trade.dto.TradeDto
import com.coing.domain.coin.trade.entity.Trade
import com.coing.domain.notification.service.PushService
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class TradeServiceTest {

    @Mock
    lateinit var tradePublisher: EventPublisher<TradeDto>

    @Mock
    lateinit var messageUtil: MessageUtil

    @Mock
    lateinit var pushService: PushService

    private lateinit var tradeService: TradeService

    private lateinit var oldTestTrade: Trade
    private lateinit var testTrade: Trade

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)

    @BeforeEach
    fun setUp() {
        tradeService = TradeService(
            eventPublisher = tradePublisher,
            messageUtil = messageUtil,
            pushService = pushService,
            coroutineScope = testScope
        )

        oldTestTrade = getTestTrade(1000.0)
        testTrade = getTestTrade(2000.0)
    }

    @AfterEach
    fun clear() {
        testScope.cancel() // 테스트 종료 시 코루틴 정리
    }

    fun getTestTrade(tradePrice: Double): Trade {
        return Trade(
            type = "trade",
            code = "KRW-BTC",
            tradePrice = tradePrice,
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
        tradeService.update(testTrade)

        // when
        val result = tradeService.getTrades(testTrade.code)

        // then
        assertNotNull(result)
        assertFalse(result.isEmpty())
        assertEquals(testTrade.code, result[0].code)
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
        val dto = TradeDto.of(testTrade, 0.0, 0.0, 0.0)

        // when
        tradeService.publish(dto)

        // then
        verify(tradePublisher, times(1)).publish(dto)
    }

    @Test
    @DisplayName("fallbackUpdate 성공")
    fun fallbackUpdate() {
        // given
        tradeService.update(testTrade)

        // when
        tradeService.fallbackUpdate("13:05:22")

        // then
        val cachedData = tradeService.getTrades("KRW-BTC")
        kotlin.test.assertTrue(cachedData.first().isFallback)
        assertEquals("13:05:22", cachedData.first().lastUpdate)

        org.mockito.kotlin.verify(tradePublisher, times(1)).publish(org.mockito.kotlin.argThat {
            isFallback && lastUpdate == "13:05:22"
        })
    }

    @Test
    @DisplayName("pushMessage 호출 시 pushService.sendAsync가 호출되는지 확인")
    fun pushMessage_Success() = runTest {
        // given
        val dto = TradeDto.of(testTrade, 0.0, 0.0, 1.0)

        // when
        `when`(messageUtil.resolveMessage("high.trade")).thenReturn("급등 알림 %s")
        tradeService.pushMessage(dto) // tradeImpact에 따라 알림 전송
        testDispatcher.scheduler.advanceUntilIdle() // 모든 코루틴이 완료될 때까지 기다림

        // then
        verify(pushService, atLeastOnce()).sendAsync(anyString(), anyString(), anyString(), anyString())
    }
}
