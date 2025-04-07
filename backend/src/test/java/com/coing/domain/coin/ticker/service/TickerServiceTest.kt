package com.coing.domain.coin.ticker.service

import com.coing.domain.coin.common.enums.AskBid
import com.coing.domain.coin.common.enums.Change
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.service.MarketService
import com.coing.domain.coin.ticker.dto.TickerDto
import com.coing.domain.coin.ticker.entity.Ticker
import com.coing.domain.coin.ticker.entity.enums.MarketState
import com.coing.domain.coin.ticker.entity.enums.MarketWarning
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.web.client.RestTemplate
import java.lang.reflect.Field
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
class TickerServiceTest {

    @Mock
    private lateinit var simpMessageSendingOperations: SimpMessageSendingOperations

    @Mock
    private lateinit var restTemplate: RestTemplate

    @Mock
    private lateinit var messageUtil: MessageUtil

    @InjectMocks
    private lateinit var tickerService: TickerService

    @Mock
    private lateinit var marketService: MarketService

    @Autowired
    private lateinit var mapper: ObjectMapper

    private lateinit var testTicker: Ticker
    private lateinit var testMarket: Market

    @BeforeEach
    fun setUp() {
        val tradeDate = LocalDate.now()
        val tradeTime = LocalTime.now()
        testTicker = Ticker(
            type = "ticker",
            code = "KRW-BTC",
            openingPrice = 100.0,
            highPrice = 120.0,
            lowPrice = 90.0,
            tradePrice = 110.0,
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
            tradeTimestamp = System.currentTimeMillis(),
            askBid = AskBid.BID,
            accAskVolume = 5000.0,
            accBidVolume = 6000.0,
            highest52WeekPrice = 130.0,
            highest52WeekDate = LocalDate.of(2024, 1, 1),
            lowest52WeekPrice = 80.0,
            lowest52WeekDate = LocalDate.of(2023, 1, 1),
            marketState = MarketState.ACTIVE,
            marketWarning = MarketWarning.NONE,
            timestamp = System.currentTimeMillis(),
            accAskBidRate = 0.0,
            highBreakout = false,
            lowBreakout = false
        )

        testMarket = Market(
            code = "KRW-BTC",
            koreanName = "비트코인",
            englishName = "Bitcoin"
        )
    }

    @Test
    @DisplayName("getTicker 성공 - 존재하는 마켓 코드 조회")
    fun getTicker_Success() {
        `when`(marketService.getCachedMarketByCode(anyString())).thenReturn(testMarket)
        tickerService.updateTicker(testTicker)

        val result = tickerService.getTicker(testTicker.code)

        assertNotNull(result)
        assertEquals(testTicker.code, result.code)
    }

    @Test
    @DisplayName("getTicker 실패 - 존재하지 않는 현재가 조회 시 예외 발생")
    fun getTicker_Failure() {
        `when`(messageUtil.resolveMessage("ticker.not.found")).thenReturn("해당 현재가를 찾을 수 없습니다.")

        val exception = assertThrows(BusinessException::class.java) {
            tickerService.getTicker("KRW-ETH")
        }

        assertEquals("해당 현재가를 찾을 수 없습니다.", exception.message)
    }

    @Test
    @DisplayName("updateTicker 성공 - 캐시에 저장 확인")
    fun updateTicker() {
        `when`(marketService.getCachedMarketByCode(anyString())).thenReturn(testMarket)
        tickerService.updateTicker(testTicker)

        val cachedTicker = getTickerCache()[testTicker.code]
        assertNotNull(cachedTicker)
        assertEquals(testTicker.code, cachedTicker?.code)
    }

    private fun getTickerCache(): Map<String, TickerDto> {
        val field: Field = TickerService::class.java.getDeclaredField("tickerCache")
        field.isAccessible = true
        return field.get(tickerService) as Map<String, TickerDto>
    }

    @Test
    @DisplayName("publishCachedTickers 성공 - WebSocket을 통해 데이터 전송")
    fun publishCachedTickers() {
        `when`(marketService.getCachedMarketByCode(anyString())).thenReturn(testMarket)
        tickerService.updateTicker(testTicker)
        val dto = TickerDto.from(testTicker, testMarket)

        tickerService.publish(dto)

        val captor = ArgumentCaptor.forClass(TickerDto::class.java)
        verify(simpMessageSendingOperations, times(1))
            .convertAndSend(eq("/sub/coin/ticker/KRW-BTC"), captor.capture())

        val sentDto = captor.value
        val actualValue = mapper.writeValueAsString(sentDto)
        val jsonNode: JsonNode = mapper.readTree(actualValue)

        assertEquals("ticker", jsonNode["type"].asText())
        assertEquals("KRW-BTC", jsonNode["code"].asText())
    }
}
