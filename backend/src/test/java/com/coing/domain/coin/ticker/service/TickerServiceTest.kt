package com.coing.domain.coin.ticker.service

import com.coing.domain.coin.common.enums.AskBid
import com.coing.domain.coin.common.enums.Change
import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.service.MarketService
import com.coing.domain.coin.ticker.dto.TickerDto
import com.coing.domain.coin.ticker.entity.Ticker
import com.coing.domain.coin.ticker.entity.enums.MarketState
import com.coing.domain.coin.ticker.entity.enums.MarketWarning
import com.coing.domain.notification.service.PushService
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.lang.reflect.Field
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@ExtendWith(MockitoExtension::class)
class TickerServiceTest {

    @Mock
    private lateinit var tickerPublisher: EventPublisher<TickerDto>

    @Mock
    private lateinit var messageUtil: MessageUtil

    @Mock
    private lateinit var marketService: MarketService

    @Mock
    private lateinit var pushService: PushService

    private lateinit var tickerService: TickerService

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)

    @Mock
    private lateinit var mapper: ObjectMapper

    private lateinit var testTicker: Ticker
    private lateinit var oldTestTicker: Ticker
    private lateinit var testMarket: Market

    @BeforeEach
    fun setUp() {
        tickerService = TickerService(
            messageUtil,
            marketService,
            tickerPublisher,
            pushService,
            testScope
        )
        mapper = ObjectMapper()
        val now = System.currentTimeMillis()
        oldTestTicker = getTestTicker(100.0, now - 61_000)
        testTicker = getTestTicker(105.0, now)
        testMarket = getTestMarket()
    }

    @AfterEach
    fun clear() {
        testScope.cancel() // 테스트 종료 시 코루틴 정리
    }

    private fun getTestMarket(): Market {
        return Market(
            code = "KRW-BTC",
            koreanName = "비트코인",
            englishName = "Bitcoin"
        )
    }

    private fun getTestTicker(tradePrice: Double, timestamp: Long): Ticker {
        val tradeDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        val tradeTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalTime()

        return Ticker(
            type = "ticker",
            code = "KRW-BTC",
            openingPrice = 100.0,
            highPrice = 120.0,
            lowPrice = 90.0,
            tradePrice = tradePrice,
            prevClosingPrice = 105.0,
            change = Change.RISE,
            changePrice = 5.0,
            signedChangePrice = 5.0,
            changeRate = 0.05,
            signedChangeRate = 0.05,
            tradeVolume = 500.0,
            accTradeVolume = 10000.0,
            accTradeVolume24h = 12000.0,
            accTradePrice = 1000000.0,
            accTradePrice24h = 1200000.0,
            tradeDate = tradeDate,
            tradeTime = tradeTime,
            tradeTimestamp = timestamp,
            askBid = AskBid.BID,
            accAskVolume = 5000.0,
            accBidVolume = 6000.0,
            highest52WeekPrice = 130.0,
            highest52WeekDate = LocalDate.of(2024, 1, 1),
            lowest52WeekPrice = 80.0,
            lowest52WeekDate = LocalDate.of(2023, 1, 1),
            marketState = MarketState.ACTIVE,
            marketWarning = MarketWarning.NONE,
            timestamp = timestamp,
            accAskBidRate = 0.0,
            highBreakout = false,
            lowBreakout = false,
            oneMinuteRate = 0.05
        )
    }

    @Test
    @DisplayName("getTicker 성공 - 존재하는 마켓 코드 조회")
    fun getTicker_Success() {
        `when`(marketService.getCachedMarketByCode(anyString())).thenReturn(testMarket)
        tickerService.update(testTicker)

        val result = tickerService.getTicker(testTicker.code)
        assertNotNull(result)
        assertEquals(testTicker.code, result.code)
    }

    @Test
    @DisplayName("getTicker 실패 - 존재하지 않는 현재가 조회 시 예외 발생")
    fun getTicker_Failure() {
        `when`(messageUtil.resolveMessage("ticker.not.found")).thenReturn("현재가 정보가 없습니다.")

        val ex = assertThrows<BusinessException> {
            tickerService.getTicker("KRW-ETH")
        }
        assertEquals(messageUtil.resolveMessage("ticker.not.found"), ex.message)
    }

    @Test
    @DisplayName("calculateOneMinuteRate 테스트")
    fun calculateOneMinuteRate() {
        `when`(marketService.getCachedMarketByCode(anyString())).thenReturn(testMarket)
        tickerService.update(oldTestTicker)

        val rate = tickerService.calculateOneMinuteRate(testTicker.code, testTicker.tradePrice)
        assertEquals(0.05, rate)
    }

    @Test
    @DisplayName("update 성공 - 캐시에 저장 확인")
    fun update() {
        `when`(marketService.getCachedMarketByCode(anyString())).thenReturn(testMarket)
        tickerService.update(testTicker)

        val deque = getTickerListCache()[testTicker.code]
        assertNotNull(deque)
        assertFalse(deque!!.isEmpty())
        assertEquals(testTicker.code, deque.peekLast().code)
    }

    private fun getTickerListCache(): Map<String, Deque<TickerDto>> {
        val field: Field = TickerService::class.java.getDeclaredField("tickerListCache")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(tickerService) as Map<String, Deque<TickerDto>>
    }

    @Test
    @DisplayName("pushMessage 성공 - 변동률이 임계값 이상이면 푸시 전송")
    fun pushMessage_Success() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()

        val ticker = getTestTicker(tradePrice = 110.0, timestamp = now)
        val dto = TickerDto.from(ticker, testMarket)

        `when`(messageUtil.resolveMessage("high.rate")).thenReturn("급등 알림 %s")

        tickerService.pushMessage(dto)
        testDispatcher.scheduler.advanceUntilIdle() // 모든 코루틴이 완료될 때까지 기다림

        verify(pushService).sendAsync(
            title = "비트코인 Bitcoin",
            body = "급등 알림 5",
            marketCode = "KRW-BTC",
            topic = "KRW-BTC-HIGH-5"
        )
    }

    @Test
    @DisplayName("publishCachedTickers 성공 - WebSocket을 통해 데이터 전송")
    fun publishCachedTickers() {
        `when`(marketService.getCachedMarketByCode(anyString())).thenReturn(testMarket)
        tickerService.update(testTicker)
        val dto = TickerDto.from(testTicker, testMarket)

        tickerService.publish(dto)

        verify(tickerPublisher, times(1))
            .publish(dto)
    }

    @Test
    @DisplayName("fallbackUpdate 성공")
    fun fallbackUpdate() {
        // given
        whenever(marketService.getCachedMarketByCode("KRW-BTC")).thenReturn(
            Market(code = "KRW-BTC", koreanName = "비트코인", englishName = "Bitcoin")
        )
        tickerService.update(testTicker)

        // when
        tickerService.fallbackUpdate("13:05:22")

        // then
        val cachedData = tickerService.getTicker("KRW-BTC")
        kotlin.test.assertTrue(cachedData.isFallback)
        assertEquals("13:05:22", cachedData.lastUpdate)

        org.mockito.kotlin.verify(tickerPublisher, times(1)).publish(org.mockito.kotlin.argThat {
            isFallback && lastUpdate == "13:05:22"
        })
    }
}
