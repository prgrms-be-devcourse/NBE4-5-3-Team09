package com.coing.domain.coin.candle.controller

import com.coing.domain.coin.candle.controller.dto.CandleResponse
import com.coing.domain.coin.candle.entity.Candle
import com.coing.domain.coin.candle.enums.EnumCandleType
import com.coing.domain.coin.candle.service.UpbitCandleService
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [CandleController::class])
@ContextConfiguration(classes = [CandleController::class])
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 비활성화
class CandleControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var upbitCandleService: UpbitCandleService

    @Test
    fun `초봉 캔들 데이터 조회`() {
        // given
        val market = "KRW-BTC"
        val type = EnumCandleType.seconds
        val dummyCandle = Candle(
                code = market,
                candleDateTimeUtc = "2023-04-07T10:00:00Z",
                open = 50000.0,
                high = 51000.0,
                low = 49500.0,
                close = 50800.0,
                volume = 100.0,
                timestamp = 1680866400000
            )
        val responseDto = listOf(CandleResponse.from(dummyCandle))
        given(upbitCandleService.getLatestCandles(market, type, null)).willReturn(listOf(dummyCandle))

        // when & then
        mockMvc.get("/api/candles/$market/$type") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(objectMapper.writeValueAsString(responseDto)) }
            jsonPath("$[0].candleDateTimeUtc", equalTo("2023-04-07T10:00:00Z"))
            jsonPath("$[0].openingPrice", equalTo(50000.0))
            jsonPath("$[0].tradePrice", equalTo(50800.0))
        }
    }

    @Test
    fun `분봉 캔들 데이터 조회 - 단위 포함`() {
        // given
        val market = "KRW-BTC"
        val type = EnumCandleType.minutes
        val unit = 5
        val dummyCandleList = listOf(
            Candle(
                code = market,
                candleDateTimeUtc = "2023-04-07T10:00:00Z",
                open = 50000.0,
                high = 51000.0,
                low = 49500.0,
                close = 50800.0,
                volume = 100.0,
                timestamp = 1680866400000
            ),
            Candle(
                code = market,
                candleDateTimeUtc = "2023-04-07T10:05:00Z",
                open = 50800.0,
                high = 51500.0,
                low = 50700.0,
                close = 51200.0,
                volume = 80.0,
                timestamp = 1680866700000
            )
        )
        val responseDtos = dummyCandleList.map { CandleResponse.from(it) }
        given(upbitCandleService.getLatestCandles(market, type, unit)).willReturn(dummyCandleList)

        // when & then
        mockMvc.get("/api/candles/$market/$type") {
            param("unit", unit.toString())
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(objectMapper.writeValueAsString(responseDtos)) }
            jsonPath("$[0].candleDateTimeUtc", equalTo("2023-04-07T10:00:00Z"))
            jsonPath("$[1].candleDateTimeUtc", equalTo("2023-04-07T10:05:00Z"))
            jsonPath("$[1].tradePrice", equalTo(51200.0))
        }
    }
}
