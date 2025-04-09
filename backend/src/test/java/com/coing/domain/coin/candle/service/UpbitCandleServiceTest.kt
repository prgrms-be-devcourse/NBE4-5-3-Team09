package com.coing.domain.coin.candle.service

import com.coing.domain.coin.candle.entity.Candle
import com.coing.domain.coin.candle.enums.EnumCandleType
import com.coing.domain.coin.candle.port.CandleDataPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant

class CandleServiceUnitTest {

	private lateinit var service: CandleService
	private lateinit var candleDataPort: CandleDataPort

	@BeforeEach
	fun setUp() {
		candleDataPort = mock(CandleDataPort::class.java)
		service = CandleService(candleDataPort)
	}

    @Test
    fun `캔들 데이터 정상 호출 - seconds`() {
        // 테스트용 캔들 데이터 배열 생성
        val testCandles = arrayOf(
            Candle(
                code = "KRW-BTC",
                candleDateTimeUtc = "2023-04-07T10:00:00Z",
                open = 100.0,
                high = 110.0,
                low = 90.0,
                close = 105.0,
                volume = 1000.0,
                timestamp = Instant.now().toEpochMilli()
            ),
            Candle(
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

        `when`(candleDataPort.fetchLatestCandles("KRW-BTC", EnumCandleType.seconds, null))
            .thenReturn(testCandles.toList())

        // when
        val result = service.getLatestCandles("KRW-BTC", EnumCandleType.seconds, null)

        // then
        assertEquals(2, result.size)
        assertEquals("2023-04-07T10:00:00Z", result[0].candleDateTimeUtc)
        assertEquals("2023-04-07T10:01:00Z", result[1].candleDateTimeUtc)
    }

    @Test
    fun `API 호출 중 예외 발생 시 빈 리스트 반환`() {
        // given
        `when`(candleDataPort.fetchLatestCandles("KRW-BTC", EnumCandleType.seconds, null))
            .thenThrow(RuntimeException("API error"))

        // when
        val result = service.getLatestCandles("KRW-BTC", EnumCandleType.seconds, null)

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `분봉 캔들 데이터 정상 호출 - minutes`() {
        val testCandles = arrayOf(
            Candle(
                code = "KRW-BTC",
                candleDateTimeUtc = "2023-04-07T10:00:00Z",
                open = 100.0,
                high = 110.0,
                low = 90.0,
                close = 105.0,
                volume = 1000.0,
                timestamp = Instant.now().toEpochMilli()
            ),
            Candle(
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
        `when`(candleDataPort.fetchLatestCandles("KRW-BTC", EnumCandleType.minutes, 5))
            .thenReturn(testCandles.toList())

        // when
        val result = service.getLatestCandles("KRW-BTC", EnumCandleType.minutes, 5)

        // then
        assertEquals(2, result.size)
        assertEquals(testCandles[0].candleDateTimeUtc, result[0].candleDateTimeUtc)
    }
}
