package com.coing.domain.coin.candle.service

import com.coing.domain.coin.candle.dto.CandleDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.lang.reflect.Field
import java.time.Instant

class UpbitCandleServiceUnitTest {

    // RestTemplate을 목(mock)으로 대체하기 위한 헬퍼 함수
    private fun setRestTemplate(service: UpbitCandleService, restTemplate: RestTemplate) {
        val field: Field = UpbitCandleService::class.java.getDeclaredField("restTemplate")
        field.isAccessible = true
        field.set(service, restTemplate)
    }

    @Test
    fun `캔들 데이터 정상 호출 - seconds`() {
        val restTemplateMock = mock(RestTemplate::class.java)
        val service = UpbitCandleService()
        setRestTemplate(service, restTemplateMock)

        // 테스트용 캔들 데이터 배열 생성
        val testCandles = arrayOf(
            CandleDto(
                code = "KRW-BTC",
                candleDateTimeUtc = "2023-04-07T10:00:00Z",
                open = 100.0,
                high = 110.0,
                low = 90.0,
                close = 105.0,
                volume = 1000.0,
                timestamp = Instant.now().toEpochMilli()
            ),
            CandleDto(
                code = "KRW-BTC",
                candleDateTimeUtc = "2023-04-07T10:01:00Z",
                open = 105.0,
                high = 115.0,
                low = 95.0,
                close = 110.0,
                volume = 1500.0,
                timestamp = Instant.now().toEpochMilli()
            )
        )

        val url = "https://api.upbit.com/v1/candles/seconds?market=KRW-BTC&count=200"
        `when`(restTemplateMock.getForEntity(url, Array<CandleDto>::class.java))
            .thenReturn(ResponseEntity(testCandles, HttpStatus.OK))

        val result = service.getLatestCandles("KRW-BTC", "seconds", null)
        assertEquals(testCandles[1].candleDateTimeUtc, result.first().candleDateTimeUtc)
        assertEquals(2, result.size)
    }

    @Test
    fun `알 수 없는 캔들 타입 - 빈 리스트 반환`() {
        val restTemplateMock = mock(RestTemplate::class.java)
        val service = UpbitCandleService()
        setRestTemplate(service, restTemplateMock)

        val result = service.getLatestCandles("KRW-BTC", "unknown", null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `API 호출 중 예외 발생 시 빈 리스트 반환`() {
        val restTemplateMock = mock(RestTemplate::class.java)
        val service = UpbitCandleService()
        setRestTemplate(service, restTemplateMock)

        val url = "https://api.upbit.com/v1/candles/seconds?market=KRW-BTC&count=200"
        `when`(restTemplateMock.getForEntity(url, Array<CandleDto>::class.java))
            .thenThrow(RuntimeException("API error"))

        val result = service.getLatestCandles("KRW-BTC", "seconds", null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `분봉 캔들 데이터 정상 호출 - minutes`() {
        val restTemplateMock = mock(RestTemplate::class.java)
        val service = UpbitCandleService()
        setRestTemplate(service, restTemplateMock)

        val testCandles = arrayOf(
            CandleDto(
                code = "KRW-BTC",
                candleDateTimeUtc = "2023-04-07T10:00:00Z",
                open = 100.0,
                high = 110.0,
                low = 90.0,
                close = 105.0,
                volume = 1000.0,
                timestamp = Instant.now().toEpochMilli()
            ),
            CandleDto(
                code = "KRW-BTC",
                candleDateTimeUtc = "2023-04-07T10:05:00Z",
                open = 105.0,
                high = 115.0,
                low = 95.0,
                close = 110.0,
                volume = 1500.0,
                timestamp = Instant.now().toEpochMilli()
            )
        )
        val url = "https://api.upbit.com/v1/candles/minutes/5?market=KRW-BTC&count=200"
        `when`(restTemplateMock.getForEntity(url, Array<CandleDto>::class.java))
            .thenReturn(ResponseEntity(testCandles, HttpStatus.OK))

        val result = service.getLatestCandles("KRW-BTC", "minutes", 5)
        assertEquals(2, result.size)
        assertEquals(testCandles[1].candleDateTimeUtc, result.first().candleDateTimeUtc)
    }
}
