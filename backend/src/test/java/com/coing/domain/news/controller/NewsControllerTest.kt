package com.coing.domain.news.controller

import com.coing.domain.coin.candle.controller.CandleController
import com.coing.domain.news.service.NewsService
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [NewsController::class])
@ContextConfiguration(classes = [NewsController::class])
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 비활성화
class NewsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var newsService: NewsService

    @Test
    fun `마켓 코드로 뉴스 조회 - 성공`() {
        // given
        val marketCode = "KRW-BTC"
        val display = 100
        val start = 1
        val sort = "sim"
        val format = "json"
        // 뉴스 서비스가 반환할 샘플 JSON 문자열
        val sampleNewsJson = """{"news": "sample news content"}"""
        given(newsService.searchNewsByMarketCode(marketCode, display, start, sort, format))
            .willReturn(sampleNewsJson)

        // when & then
        mockMvc.get("/api/news") {
            param("market", marketCode)
            param("display", display.toString())
            param("start", start.toString())
            param("sort", sort)
            param("format", format)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(sampleNewsJson) }
        }
    }
}
