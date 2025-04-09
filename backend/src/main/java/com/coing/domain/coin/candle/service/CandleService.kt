package com.coing.domain.coin.candle.service

import com.coing.domain.coin.candle.entity.Candle
import com.coing.domain.coin.candle.enums.EnumCandleType
import com.coing.domain.coin.candle.port.CandleDataPort
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
	fun getLatestCandles(market: String, candleType: EnumCandleType, unit: Int?): List<Candle> {
		return try {
			candleDataPort.fetchLatestCandles(market, candleType, unit)
		} catch  (e: Exception) {
			log.error("[Candle] Error fetching: ${e.message}.")
			emptyList()
		}
	}
}
