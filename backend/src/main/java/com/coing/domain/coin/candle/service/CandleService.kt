package com.coing.domain.coin.candle.service

import com.coing.domain.coin.candle.entity.Candle
import com.coing.domain.coin.candle.enums.EnumCandleType
import com.coing.domain.coin.candle.port.CandleDataPort
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class CandleService(
	private val candleDataPort: CandleDataPort
) {
	private val log = LoggerFactory.getLogger(CandleService::class.java)

	/**
	 * 특정 마켓과 캔들 타입에 따른 보정된 캔들 데이터를 Upbit REST API로부터 가져옵니다.
	 * 분봉의 경우 unit 파라미터를 사용합니다.
	 */
    @Retry(name = "upbit-rest-api")
	@CircuitBreaker(name = "upbit-rest-api", fallbackMethod = "fallbackGetLatestCandles")
	fun getLatestCandles(market: String, candleType: EnumCandleType, unit: Int?): List<Candle> {
		log.info("[CandleService] fetchLatestCandles 호출됨")
        return candleDataPort.fetchLatestCandles(market, candleType, unit)
	}

    fun fallbackGetLatestCandles(market: String, candleType: EnumCandleType, unit: Int?, t: Throwable): List<Candle> {
        log.error("[Candle] Error fetching candles: ${t.message}. Returning empty fallback list.")
        return emptyList() // 또는 DB/캐시 데이터를 반환하는 로직
    }
}
