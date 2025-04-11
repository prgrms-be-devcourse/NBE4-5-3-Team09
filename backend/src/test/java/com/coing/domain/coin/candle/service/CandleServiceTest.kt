package com.coing.domain.coin.candle.service

import com.coing.domain.coin.candle.entity.Candle
import com.coing.domain.coin.candle.enums.EnumCandleType
import com.coing.domain.coin.candle.port.CandleDataPort
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Instant

@SpringBootTest
@TestPropertySource(properties = [
    "resilience4j.retry.instances.upbit-rest-api.max-attempts=3",
    "resilience4j.retry.instances.upbit-rest-api.wait-duration=100ms",
    "resilience4j.retry.instances.upbit-rest-api.enable-exponential-backoff=false",
    "resilience4j.circuitbreaker.instances.upbit-rest-api.sliding-window-size=10",
    "resilience4j.circuitbreaker.instances.upbit-rest-api.failure-rate-threshold=50",
    "resilience4j.circuitbreaker.instances.upbit-rest-api.minimum-number-of-calls=3",
    "resilience4j.circuitbreaker.instances.upbit-rest-api.wait-duration-in-open-state=5s"
])
class CandleServiceTest {
    @MockitoBean
	private lateinit var candleDataPort: CandleDataPort
    @Autowired
	private lateinit var service: CandleService
    @Autowired
    lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    private val market = "KRW-BTC"
    private val candleType = EnumCandleType.minutes
    private val unit = 1

    @BeforeEach
    fun reset() {
        circuitBreakerRegistry.circuitBreaker("upbit-rest-api").reset()
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
        whenever(candleDataPort.fetchLatestCandles("KRW-BTC", EnumCandleType.seconds, null))
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
        whenever(candleDataPort.fetchLatestCandles(eq(market), eq(candleType), eq(unit)))
            .thenAnswer { throw RuntimeException("fail") }

        // when
        val result = service.getLatestCandles(market, candleType, unit)

        // then
        assertEquals(emptyList<Candle>(), result)
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
