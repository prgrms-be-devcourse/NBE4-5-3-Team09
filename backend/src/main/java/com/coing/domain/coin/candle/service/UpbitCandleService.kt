package com.coing.domain.coin.candle.service

import com.coing.domain.coin.candle.dto.CandleDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UpbitCandleService {
	private val restTemplate: RestTemplate = RestTemplate()
	private val log = LoggerFactory.getLogger(UpbitCandleService::class.java)

	/**
	 * 특정 마켓과 캔들 타입에 따른 보정된 캔들 데이터를 Upbit REST API로부터 가져옵니다.
	 * 분봉의 경우 unit 파라미터를 사용합니다.
	 */
	fun getLatestCandles(market: String, candleType: String, unit: Int?): List<CandleDto> {
		val url = when (candleType.lowercase()) {
			"seconds" -> "https://api.upbit.com/v1/candles/seconds?market=$market&count=200"
			"minutes" -> {
				val minuteUnit = unit ?: 1
				"https://api.upbit.com/v1/candles/minutes/$minuteUnit?market=$market&count=200"
			}
			"days" -> "https://api.upbit.com/v1/candles/days?market=$market&count=200"
			"weeks" -> "https://api.upbit.com/v1/candles/weeks?market=$market&count=200"
			"months" -> "https://api.upbit.com/v1/candles/months?market=$market&count=200"
			"years" -> "https://api.upbit.com/v1/candles/years?market=$market&count=200"
			else -> {
				log.warn("알 수 없는 캔들 타입: {}", candleType)
				return emptyList()
			}
		}

		return try {
			val response: ResponseEntity<Array<CandleDto>> =
				restTemplate.getForEntity(url, Array<CandleDto>::class.java)
			if (response.statusCode == HttpStatus.OK && response.body != null) {
				val candles = response.body!!
				// API는 최신 캔들을 내림차순으로 반환할 수 있으므로, 오름차순 정렬
				val candleList = candles.toList().toMutableList()
				candleList.reverse()
				candleList
			} else {
				log.warn("Upbit candle API 호출 실패: {}", response.statusCode)
				emptyList()
			}
		} catch (e: Exception) {
			log.error("Upbit candle API 호출 중 오류 발생", e)
			emptyList()
		}
	}
}
