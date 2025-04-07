package com.coing.domain.coin.candle.controller

import com.coing.domain.coin.candle.dto.CandleDto
import com.coing.domain.coin.candle.service.UpbitCandleService
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
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

    @MockBean
    lateinit var upbitCandleService: UpbitCandleService

    @Test
    fun `초봉 캔들 데이터 조회`() {
        // given
        val market = "KRW-BTC"
        val type = "seconds"
        val dummyCandleList = listOf(
            CandleDto(
                code = market,
                candleDateTimeUtc = "2023-04-07T10:00:00Z",
                open = 50000.0,
                high = 51000.0,
                low = 49500.0,
                close = 50800.0,
                volume = 100.0,
                timestamp = 1680866400000
            )
        )
        given(upbitCandleService.getLatestCandles(market, type, null)).willReturn(dummyCandleList)

        // when & then
        mockMvc.get("/api/candles/$market/$type") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(objectMapper.writeValueAsString(dummyCandleList)) }
            jsonPath("$[0].candle_date_time_utc", equalTo("2023-04-07T10:00:00Z"))
        }
    }

    @Test
    fun `분봉 캔들 데이터 조회 - 단위 포함`() {
        // given
        val market = "KRW-BTC"
        val type = "minutes"
        val unit = 5
        val dummyCandleList = listOf(
            CandleDto(
                code = market,
                candleDateTimeUtc = "2023-04-07T10:00:00Z",
                open = 50000.0,
                high = 51000.0,
                low = 49500.0,
                close = 50800.0,
                volume = 100.0,
                timestamp = 1680866400000
            ),
            CandleDto(
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
        given(upbitCandleService.getLatestCandles(market, type, unit)).willReturn(dummyCandleList)

        // when & then
        mockMvc.get("/api/candles/$market/$type") {
            param("unit", unit.toString())
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(objectMapper.writeValueAsString(dummyCandleList)) }
            jsonPath("$[0].candle_date_time_utc", equalTo("2023-04-07T10:00:00Z"))
            jsonPath("$[1].candle_date_time_utc", equalTo("2023-04-07T10:05:00Z"))
        }
    }
}
